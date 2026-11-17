package com.codelens.ai;

import com.codelens.ai.config.DotenvEnvironmentPostProcessor;
import com.codelens.ai.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class JwtTokenProviderTest {

    @BeforeAll
    public static void setUp() {
        DotenvEnvironmentPostProcessor.loadIntoSystemProperties();
    }

    @Test
    public void testJwtGenerationAndValidationWithDotenvSecret() {
        Map<String, Object> props = DotenvEnvironmentPostProcessor.loadDotenvProperties();
        String secret = (String) props.get("JWT_SECRET");
        assertNotNull(secret, "JWT_SECRET must be present in .env");

        JwtTokenProvider provider = new JwtTokenProvider();
        ReflectionTestUtils.setField(provider, "jwtSecret", secret);
        ReflectionTestUtils.setField(provider, "jwtExpirationInMs", 86400000);

        Authentication auth = new UsernamePasswordAuthenticationToken("testuser", null, Collections.emptyList());
        String token = provider.generateToken(auth);

        assertNotNull(token);
        assertTrue(token.length() > 20);
        assertTrue(provider.validateToken(token));
        assertEquals("testuser", provider.getUsernameFromJWT(token));
    }
}
