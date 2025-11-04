package com.yashaswi.weatherapi.service;

import com.yashaswi.weatherapi.config.WeatherApiProperties;
import com.yashaswi.weatherapi.exception.ExternalApiException;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;

class WeatherServiceTest {

    private static MockWebServer mockServer;
    private WeatherService weatherService;

    @BeforeAll
    static void setupMockServer() throws IOException {
        mockServer = new MockWebServer();
        mockServer.start();
    }

    @AfterAll
    static void shutdownMockServer() throws IOException {
        mockServer.shutdown();
    }

    @BeforeEach
    void setup() {
        WeatherApiProperties props = new WeatherApiProperties();
        props.setBaseUrl(mockServer.url("/").toString());
        props.setApiKey("dummy-key");
        weatherService = new WeatherService(props, WebClient.builder());
    }

    @Test
    void shouldReturnMappedWeatherResponse() {
        String mockResponse = """
            {
              "name": "London",
              "main": { "temp": 22.5, "humidity": 60, "pressure": 1012 },
              "weather": [{ "description": "sunny", "icon": "01d" }],
              "wind": { "speed": 3.2 }
            }
            """;
        mockServer.enqueue(new MockResponse()
                .setBody(mockResponse)
                .addHeader("Content-Type", "application/json"));

        StepVerifier.create(weatherService.getWeatherByCity("London"))
                .assertNext(response -> {
                    Assertions.assertEquals("London", response.getCity());
                    Assertions.assertEquals("sunny", response.getDescription());
                    Assertions.assertEquals(22.5, response.getTemperature());
                })
                .verifyComplete();
    }
    @Test
    void shouldThrowWeatherNotFoundException_when404() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(404)
                .setBody("{\"message\":\"city not found\"}"));

        StepVerifier.create(weatherService.getWeatherByCity("InvalidCity"))
                .expectError(WeatherNotFoundException.class)
                .verify();
    }

    @Test
    void shouldThrowExternalApiException_when500() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(500)
                .setBody("{\"error\":\"Internal Server Error\"}"));

        StepVerifier.create(weatherService.getWeatherByCity("London"))
                .expectError(ExternalApiException.class)
                .verify();
    }

    @Test
    void shouldThrowExternalApiException_when401() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(401)
                .setBody("{\"error\":\"Unauthorized\"}"));

        StepVerifier.create(weatherService.getWeatherByCity("London"))
                .expectError(ExternalApiException.class)
                .verify();
    }

    @Test
    void shouldThrowExternalApiException_when429RateLimited() {
        mockServer.enqueue(new MockResponse()
                .setResponseCode(429)
                .setBody("{\"error\":\"Rate limit exceeded\"}"));

        StepVerifier.create(weatherService.getWeatherByCity("London"))
                .expectError(ExternalApiException.class)
                .verify();
    }

}
