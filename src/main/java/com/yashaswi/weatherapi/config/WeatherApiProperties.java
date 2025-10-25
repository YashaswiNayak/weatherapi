package com.yashaswi.weatherapi.config;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Data
@Validated
public class WeatherApiProperties {

    /**
     * OpenWeatherMap API key (required)
     */
    @NotBlank(message = "Weather API key must be configured")
    private String apiKey;

    /**
     * Base URL for OpenWeatherMap API
     */
    @NotBlank(message = "Weather API base URL must be configured")
    private String baseUrl = "https://api.openweathermap.org/data/2.5";

    /**
     * Request timeout duration
     */
    @NotNull
    private Duration timeout = Duration.ofSeconds(10);

    /**
     * Number of retry attempts for failed requests
     */
    private int retryAttempts = 3;

    /**
     * Cache configuration
     */
    private Cache cache = new Cache();

    @Data
    public static class Cache {
        /**
         * Time-to-live for cached weather data
         */
        private Duration ttl = Duration.ofHours(12);

        /**
         * Redis key prefix for weather cache entries
         */
        private String keyPrefix = "weather";

        /**
         * Enable/disable caching
         */
        private boolean enabled = true;
    }
}
