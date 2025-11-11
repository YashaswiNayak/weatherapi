package com.yashaswi.weatherapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

/**
 * Configuration properties for Weather API integration.
 * Maps properties from application.yml with prefix "weather.api".
 */
@Component
@ConfigurationProperties(prefix = "weather.api")
@Validated  // Enables JSR-303 validation
@Data
public class WeatherApiProperties {

    /**
     * API key for OpenWeatherMap API.
     * Must be set via environment variable or application.yml.
     */
    @NotBlank(message = "weather.api.api-key must be configured")
    private String apiKey;

    /**
     * Base URL for Weather API.
     * Default: https://api.openweathermap.org/data/2.5
     */
    @NotBlank(message = "weather.api.base-url must be configured")
    private String baseUrl = "https://api.openweathermap.org/data/2.5";

    /**
     * Connection timeout in milliseconds.
     * Default: 5000ms (5 seconds)
     */
    @Positive
    private int connectTimeout = 5000;

    /**
     * Read timeout in milliseconds.
     * Default: 5000ms (5 seconds)
     */
    @Positive
    private int readTimeout = 5000;
}
