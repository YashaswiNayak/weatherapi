package com.yashaswi.weatherapi.controller;

import com.yashaswi.weatherapi.dtos.WeatherResponse;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import com.yashaswi.weatherapi.service.WeatherService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/weather")
@Validated
@Slf4j
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping
    public Mono<ResponseEntity<WeatherResponse>> getWeather(@RequestParam String city) {
        log.info("Received weather request for city: {}", city);
        return weatherService.getWeatherByCity(city)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.error(new WeatherNotFoundException("Weather data not available for city: " + city)));
    }
}
