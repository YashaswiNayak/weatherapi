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
    @NotBlank(message = "Weather API key must be configured")
    private String apiKey;
    @NotBlank(message = "Weather API base URL must be configured")
    private String baseUrl = "https://api.openweathermap.org/data/2.5";
    @NotNull
    private Duration timeout = Duration.ofSeconds(10);
    private int retryAttempts = 3;
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
