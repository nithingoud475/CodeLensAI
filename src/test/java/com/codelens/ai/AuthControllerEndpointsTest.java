package com.codelens.ai;

import com.codelens.ai.controller.AuthController;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class AuthControllerEndpointsTest {

    @Test
    public void testEmailLoginEndpointsRemoved() {
        Method[] methods = AuthController.class.getDeclaredMethods();
        List<String> mappings = new ArrayList<>();

        for (Method method : methods) {
            assertNotEquals("sendLoginOtp", method.getName(), "sendLoginOtp method must be removed");
            assertNotEquals("loginWithOtp", method.getName(), "loginWithOtp method must be removed");

            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping post = method.getAnnotation(PostMapping.class);
                for (String path : post.value()) {
                    mappings.add(path);
                }
            }
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping get = method.getAnnotation(GetMapping.class);
                for (String path : get.value()) {
                    mappings.add(path);
                }
            }
        }

        assertFalse(mappings.contains("/send-login-otp"), "POST /send-login-otp must be removed");
        assertFalse(mappings.contains("/login-with-otp"), "POST /login-with-otp must be removed");
    }

    @Test
    public void testPreservedEndpointsExist() {
        Method[] methods = AuthController.class.getDeclaredMethods();
        List<String> mappings = new ArrayList<>();

        for (Method method : methods) {
            if (method.isAnnotationPresent(PostMapping.class)) {
                PostMapping post = method.getAnnotation(PostMapping.class);
                for (String path : post.value()) {
                    mappings.add(path);
                }
            }
            if (method.isAnnotationPresent(GetMapping.class)) {
                GetMapping get = method.getAnnotation(GetMapping.class);
                for (String path : get.value()) {
                    mappings.add(path);
                }
            }
        }

        assertTrue(mappings.contains("/login"), "POST /login must be preserved");
        assertTrue(mappings.contains("/register"), "POST /register must be preserved");
        assertTrue(mappings.contains("/verify-otp"), "POST /verify-otp must be preserved");
        assertTrue(mappings.contains("/resend-otp"), "POST /resend-otp must be preserved");
        assertTrue(mappings.contains("/google"), "POST /google must be preserved");
        assertTrue(mappings.contains("/google-client-id"), "GET /google-client-id must be preserved");
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testGoogleClientIdEndpointPreservedAndDoesNotExposeSecret() {
        AuthController controller = new AuthController();
        String testClientId = "test-client-id.apps.googleusercontent.com";
        String testClientSecret = "test-client-secret";

        ReflectionTestUtils.setField(controller, "googleClientId", testClientId);
        ReflectionTestUtils.setField(controller, "googleClientSecret", testClientSecret);

        ResponseEntity<?> response = controller.getGoogleClientId();
        assertEquals(200, response.getStatusCode().value());

        Map<String, String> body = (Map<String, String>) response.getBody();
        assertNotNull(body);
        assertEquals(testClientId, body.get("clientId"));
        assertFalse(body.containsKey("clientSecret"));
        assertFalse(body.containsKey("secret"));
    }
}
