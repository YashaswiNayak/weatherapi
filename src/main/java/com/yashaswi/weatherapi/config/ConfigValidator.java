package com.yashaswi.weatherapi.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("!test")  // Skip validation in test environment
@Slf4j
public class ConfigValidator {

    @Value("${weather.api.api-key:}")
    private String apiKey;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @PostConstruct
    public void validateConfiguration() {
        log.info("=== Configuration Validation (Profile: {}) ===", activeProfile);

        // Validate API key is set and not a placeholder
        if (apiKey == null || apiKey.isEmpty() || apiKey.contains("your-") || apiKey.contains("placeholder")) {
            log.error("WEATHER_API_KEY is not properly configured!");
            throw new IllegalStateException(
                    "WEATHER_API_KEY environment variable is not set or contains placeholder value. " +
                            "Please set a valid OpenWeatherMap API key in your .env file or environment."
            );
        }

        log.info("✅ Configuration validation passed");
        log.info("API Key: {}***{}",
                apiKey.substring(0, Math.min(4, apiKey.length())),
                apiKey.substring(Math.max(0, apiKey.length() - 4)));
    }
}
