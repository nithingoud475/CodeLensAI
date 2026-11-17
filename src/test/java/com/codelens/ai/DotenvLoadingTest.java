package com.codelens.ai;

import com.codelens.ai.config.DotenvEnvironmentPostProcessor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DotenvLoadingTest {

    @BeforeAll
    public static void setUp() {
        DotenvEnvironmentPostProcessor.loadIntoSystemProperties();
    }

    @Test
    public void testDotenvPropertiesLoaded() {
        Map<String, Object> props = DotenvEnvironmentPostProcessor.loadDotenvProperties();
        assertFalse(props.isEmpty(), ".env should be found and loaded");

        assertTrue(props.containsKey("GOOGLE_CLIENT_ID"), "Missing GOOGLE_CLIENT_ID");
        assertTrue(props.containsKey("GOOGLE_CLIENT_SECRET"), "Missing GOOGLE_CLIENT_SECRET");
        assertTrue(props.containsKey("MAIL_USERNAME"), "Missing MAIL_USERNAME");
        assertTrue(props.containsKey("MAIL_PASSWORD"), "Missing MAIL_PASSWORD");
        assertTrue(props.containsKey("JWT_SECRET"), "Missing JWT_SECRET");
        assertTrue(props.containsKey("DB_USERNAME"), "Missing DB_USERNAME");
        assertTrue(props.containsKey("DB_PASSWORD"), "Missing DB_PASSWORD");

        // Verify values are populated and non-empty
        assertNotNull(props.get("GOOGLE_CLIENT_ID"));
        assertNotNull(props.get("GOOGLE_CLIENT_SECRET"));
        assertNotNull(props.get("MAIL_USERNAME"));
        assertNotNull(props.get("MAIL_PASSWORD"));
        assertNotNull(props.get("JWT_SECRET"));
        assertNotNull(props.get("DB_USERNAME"));
        assertNotNull(props.get("DB_PASSWORD"));

        // Verify System properties are also populated
        assertNotNull(System.getProperty("GOOGLE_CLIENT_ID"));
        assertNotNull(System.getProperty("JWT_SECRET"));
        assertNotNull(System.getProperty("DB_USERNAME"));
    }

    @Test
    public void testEnvironmentPostProcessorRegistersSource() {
        DotenvEnvironmentPostProcessor processor = new DotenvEnvironmentPostProcessor();
        org.springframework.core.env.StandardEnvironment environment = new org.springframework.core.env.StandardEnvironment();
        processor.postProcessEnvironment(environment, new org.springframework.boot.SpringApplication());

        org.springframework.core.env.PropertySource<?> source = environment.getPropertySources().get(DotenvEnvironmentPostProcessor.PROPERTY_SOURCE_NAME);
        assertNotNull(source, "Property source dotenvProperties should be registered in Environment");
        assertNotNull(source.getProperty("GOOGLE_CLIENT_ID"));
        assertNotNull(source.getProperty("GOOGLE_CLIENT_SECRET"));
        assertNotNull(source.getProperty("JWT_SECRET"));
        assertNotNull(source.getProperty("DB_PASSWORD"));
        assertNotNull(source.getProperty("MAIL_USERNAME"));
        assertNotNull(source.getProperty("MAIL_PASSWORD"));
    }
}
