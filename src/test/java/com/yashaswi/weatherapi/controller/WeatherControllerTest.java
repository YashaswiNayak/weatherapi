package com.yashaswi.weatherapi.controller;

import com.yashaswi.weatherapi.dto.WeatherResponse;
import com.yashaswi.weatherapi.exception.CacheUnavailableException;
import com.yashaswi.weatherapi.exception.ExternalApiException;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import com.yashaswi.weatherapi.service.WeatherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

    @Test
    void shouldReturn502ForExternalApiError() {
        when(weatherService.getWeatherByCity(anyString()))
                .thenReturn(Mono.error(new ExternalApiException("Upstream API error (500)")));

        webTestClient.get()
                .uri("/api/weather?city=London")
                .exchange()
                .expectStatus().isEqualTo(502)
                .expectBody()
                .jsonPath("$.status").isEqualTo(502)
                .jsonPath("$.error").isEqualTo("EXTERNAL_API_ERROR")
                .jsonPath("$.message").exists()
                .jsonPath("$.path").isEqualTo("/api/weather");
    }

    @Test
    void shouldReturn503ForCacheUnavailable() {
        when(weatherService.getWeatherByCity(anyString()))
                .thenReturn(Mono.error(new CacheUnavailableException("Cache unavailable")));

        webTestClient.get()
                .uri("/api/weather?city=London")
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.status").isEqualTo(503)
                .jsonPath("$.error").isEqualTo("CACHE_UNAVAILABLE")
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldReturn400ForMissingCityParameter() {
        webTestClient.get()
                .uri("/api/weather")  // Missing city parameter
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.error").exists()
                .jsonPath("$.message").exists();
    }

    @Test
    void shouldReturn404WithCorrectErrorStructure() {
        when(weatherService.getWeatherByCity(anyString()))
                .thenReturn(Mono.error(new WeatherNotFoundException("City not found")));

        webTestClient.get()
                .uri("/api/weather?city=UnknownCity")
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.status").isEqualTo(404)
                .jsonPath("$.error").isEqualTo("Weather Not Found")
                .jsonPath("$.message").isEqualTo("City not found")
                .jsonPath("$.path").isEqualTo("/api/weather");
    }

}
