package com.yashaswi.weatherapi.controller;

import com.yashaswi.weatherapi.dtos.WeatherResponse;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("api/weather")
@Validated
@Slf4j
public class WeatherController {
    private static final Map<String, WeatherResponse> MOCK_WEATHER_DATA = new HashMap<>();

    static {
        MOCK_WEATHER_DATA.put("london", WeatherResponse.builder()
                .city("London")
                .country("UK")
                .temperature(15.5)
                .description("Partly cloudy")
                .humidity(72)
                .windSpeed(3.2)
                .pressure(1013)
                .icon("partly-cloudy")
                .build());

        MOCK_WEATHER_DATA.put("new york", WeatherResponse.builder()
                .city("New York")
                .country("US")
                .temperature(18.3)
                .description("Sunny")
                .humidity(45)
                .windSpeed(2.1)
                .pressure(1018)
                .icon("sunny")
                .build());

        MOCK_WEATHER_DATA.put("tokyo", WeatherResponse.builder()
                .city("Tokyo")
                .country("JP")
                .temperature(22.1)
                .description("Rainy")
                .humidity(85)
                .windSpeed(1.8)
                .pressure(1005)
                .icon("rainy")
                .build());
    }
    @GetMapping
    public ResponseEntity<WeatherResponse> getWeather(@RequestParam String city){
        log.info("Recieved weather request for city : {}",city);

        WeatherResponse weatherData=MOCK_WEATHER_DATA.get(city.toLowerCase());

        if(weatherData==null){
            log.warn("Weather data not found for city: {}",city);
            throw new WeatherNotFoundException("Weather data not available for city: " + city);
        }

        log.info("Returning weather data for city: {}",city);
        return ResponseEntity.ok(weatherData);
    }

    @GetMapping("/cities")
    public ResponseEntity<Map<String, String>> getAvailableCities() {
        Map<String, String> cities = new HashMap<>();
        MOCK_WEATHER_DATA.forEach((key, value) ->
                cities.put(key, value.getCity() + ", " + value.getCountry()));

        return ResponseEntity.ok(cities);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "weather-api-service");
        status.put("version", "1.0.0-PHASE0");
        return ResponseEntity.ok(status);
    }
    
}
