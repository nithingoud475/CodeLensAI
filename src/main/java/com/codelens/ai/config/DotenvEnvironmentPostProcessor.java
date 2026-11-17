package com.codelens.ai.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    public static final String PROPERTY_SOURCE_NAME = "dotenvProperties";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> envProperties = loadDotenvProperties();
        if (!envProperties.isEmpty()) {
            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, envProperties));
            for (Map.Entry<String, Object> entry : envProperties.entrySet()) {
                if (System.getProperty(entry.getKey()) == null && entry.getValue() != null) {
                    System.setProperty(entry.getKey(), entry.getValue().toString());
                }
            }
        }
    }

    public static Map<String, Object> loadDotenvProperties() {
        Map<String, Object> envMap = new HashMap<>();

        Path[] potentialPaths = new Path[]{
                Paths.get(".env"),
                Paths.get(System.getProperty("user.dir", "."), ".env"),
                Paths.get("..", ".env")
        };

        Path envPath = null;
        for (Path path : potentialPaths) {
            if (Files.isRegularFile(path)) {
                envPath = path;
                break;
            }
        }

        if (envPath == null) {
            return envMap;
        }

        try (BufferedReader reader = Files.newBufferedReader(envPath, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();

                    if ((value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) ||
                        (value.startsWith("'") && value.endsWith("'") && value.length() >= 2)) {
                        value = value.substring(1, value.length() - 1);
                    }

                    if (!key.isEmpty()) {
                        envMap.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            // Silently handle if .env cannot be read
        }

        return envMap;
    }

    public static void loadIntoSystemProperties() {
        Map<String, Object> properties = loadDotenvProperties();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (System.getProperty(entry.getKey()) == null && entry.getValue() != null) {
                System.setProperty(entry.getKey(), entry.getValue().toString());
            }
        }
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
