package com.codelens.ai.controller;

import com.codelens.ai.dto.AuthResponse;
import com.codelens.ai.dto.LoginRequest;
import com.codelens.ai.dto.RegisterRequest;
import com.codelens.ai.entity.OtpToken;
import com.codelens.ai.entity.User;
import com.codelens.ai.repository.OtpRepository;
import com.codelens.ai.repository.UserRepository;
import com.codelens.ai.security.JwtTokenProvider;
import com.codelens.ai.service.EmailService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private EmailService emailService;

    @Value("${app.google.client-id:}")
    private String googleClientId;

    private final SecureRandom secureRandom = new SecureRandom();

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        String usernameOrEmail = loginRequest.getUsername();
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        
        if (userOpt.isPresent() && !userOpt.get().isVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Email not verified. Please verify your email."));
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        usernameOrEmail,
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = tokenProvider.generateToken(authentication);
        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        if (!registerRequest.getEmail().endsWith("@gmail.com")) {
            return ResponseEntity.badRequest().body(Map.of("message", "Only @gmail.com addresses are allowed."));
        }

        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username is already taken!"));
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email Address already in use!"));
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setVerified(false);
        user.setProvider("LOCAL");
        userRepository.save(user);

        generateAndSendOtp(user.getEmail());

        return ResponseEntity.ok(Map.of("message", "Registration successful. Please verify your email."));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required."));
        }

        Optional<OtpToken> otpOpt = otpRepository.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP."));
        }

        OtpToken otpToken = otpOpt.get();
        if (otpToken.getAttempts() >= 5) {
            otpRepository.delete(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Too many attempts. Please request a new OTP."));
        }

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "OTP expired."));
        }

        if (!passwordEncoder.matches(otp, otpToken.getOtpHash())) {
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpRepository.save(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid OTP."));
        }

        // OTP is correct
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setVerified(true);
            userRepository.save(user);
        }

        otpRepository.delete(otpToken);
        return ResponseEntity.ok(Map.of("message", "Email verified successfully."));
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty() || userOpt.get().isVerified()) {
            return ResponseEntity.badRequest().body(Map.of("message", "User not found or already verified."));
        }
        
        Optional<OtpToken> existingOtp = otpRepository.findByEmail(email);
        if (existingOtp.isPresent()) {
            if (existingOtp.get().getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please wait before requesting a new OTP."));
            }
        }

        generateAndSendOtp(email);
        return ResponseEntity.ok(Map.of("message", "OTP sent."));
    }

    @PostMapping("/send-login-otp")
    public ResponseEntity<?> sendLoginOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email is required."));
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No account found with this email."));
        }
        
        Optional<OtpToken> existingOtp = otpRepository.findByEmail(email);
        if (existingOtp.isPresent()) {
            if (existingOtp.get().getCreatedAt().plusSeconds(60).isAfter(LocalDateTime.now())) {
                return ResponseEntity.badRequest().body(Map.of("message", "Please wait before requesting a new OTP."));
            }
        }

        generateAndSendOtp(email);
        return ResponseEntity.ok(Map.of("message", "Login OTP sent."));
    }

    @PostMapping("/login-with-otp")
    public ResponseEntity<?> loginWithOtp(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        String otp = payload.get("otp");

        if (email == null || otp == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email and OTP are required."));
        }

        Optional<OtpToken> otpOpt = otpRepository.findByEmail(email);
        if (otpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP."));
        }

        OtpToken otpToken = otpOpt.get();
        if (otpToken.getAttempts() >= 5) {
            otpRepository.delete(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Too many attempts. Please request a new OTP."));
        }

        if (otpToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpRepository.delete(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "OTP expired. Please request a new code."));
        }

        if (!passwordEncoder.matches(otp, otpToken.getOtpHash())) {
            otpToken.setAttempts(otpToken.getAttempts() + 1);
            otpRepository.save(otpToken);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid verification code."));
        }

        // OTP is correct
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "No account found with this email."));
        }

        User user = userOpt.get();
        if (!user.isVerified()) {
            user.setVerified(true);
            userRepository.save(user);
        }

        otpRepository.delete(otpToken);

        // Generate JWT
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new AuthResponse(jwt));
    }

    @PostMapping("/google")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> payload) {
        String idTokenString = payload.get("token");
        if (idTokenString == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Token required"));
        }

        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken != null) {
                GoogleIdToken.Payload googlePayload = idToken.getPayload();
                String email = googlePayload.getEmail();
                String name = (String) googlePayload.get("name");

                Optional<User> userOpt = userRepository.findByEmail(email);
                User user;
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                    if (!user.isVerified()) {
                        user.setVerified(true);
                        userRepository.save(user);
                    }
                } else {
                    user = new User();
                    user.setUsername(email.split("@")[0] + "_" + secureRandom.nextInt(1000));
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode("GOOGLE_AUTH_" + secureRandom.nextLong()));
                    user.setVerified(true);
                    user.setProvider("GOOGLE");
                    userRepository.save(user);
                }

                // Create Authentication object
                Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                String jwt = tokenProvider.generateToken(authentication);

                return ResponseEntity.ok(new AuthResponse(jwt));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid ID token."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Google auth failed."));
        }
    }

    private void generateAndSendOtp(String email) {
        otpRepository.deleteByEmail(email);

        String otp = String.format("%06d", secureRandom.nextInt(999999));
        OtpToken otpToken = new OtpToken();
        otpToken.setEmail(email);
        otpToken.setOtpHash(passwordEncoder.encode(otp));
        otpToken.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        otpRepository.save(otpToken);

        emailService.sendOtpEmail(email, otp);
    }

    @GetMapping("/google-client-id")
    public ResponseEntity<?> getGoogleClientId() {
        return ResponseEntity.ok(Map.of("clientId", googleClientId));
    }
}
