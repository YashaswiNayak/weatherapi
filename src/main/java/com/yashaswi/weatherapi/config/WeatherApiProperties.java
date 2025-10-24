package com.yashaswi.weatherapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "weather.api")
@Data
public class WeatherApiProperties {
    private String apiKey;
    private String baseUrl;
}
