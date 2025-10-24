package com.yashaswi.weatherapi.controller;

import com.yashaswi.weatherapi.dtos.WeatherResponse;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import com.yashaswi.weatherapi.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
class WeatherControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private WeatherService weatherService;

    @Test
    void shouldReturnWeatherForValidCity() {
        WeatherResponse mockResponse = WeatherResponse.builder()
                .city("London")
                .country("UK")
                .temperature(15.5)
                .description("cloudy")
                .humidity(65)
                .pressure(1012)
                .windSpeed(3.5)
                .icon("02d")
                .source("weatherapi")
                .build();

        when(weatherService.getWeatherByCity("London"))
                .thenReturn(Mono.just(mockResponse));

        webTestClient.get()
                .uri("/api/weather?city=London")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.city").isEqualTo("London")
                .jsonPath("$.description").isEqualTo("cloudy")
                .jsonPath("$.temperature").isEqualTo(15.5);
    }

    @Test
    void shouldReturn404ForInvalidCity() {
        when(weatherService.getWeatherByCity(anyString()))
                .thenReturn(Mono.error(new WeatherNotFoundException("City not found")));

        webTestClient.get()
                .uri("/api/weather?city=UnknownCity")
                .exchange()
                .expectStatus().isNotFound();
    }
}
