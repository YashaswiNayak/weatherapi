package com.yashaswi.weatherapi.service;

import com.yashaswi.weatherapi.config.WeatherApiProperties;
import com.yashaswi.weatherapi.dtos.OpenWeatherMapResponse;
import com.yashaswi.weatherapi.dtos.WeatherResponse;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class WeatherService {
    private final WebClient webClient;
    private final WeatherApiProperties weatherApiProperties;

    public WeatherService(WeatherApiProperties weatherApiProperties, WebClient.Builder webClient) {
        this.weatherApiProperties = weatherApiProperties;
        this.webClient = webClient.baseUrl(weatherApiProperties.getBaseUrl()).build();
    }

    public Mono<WeatherResponse> getWeatherByCity(String city) {
        String uri = String.format("/weather?q=%s&appid=%s&units=metric", city, weatherApiProperties.getApiKey());

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OpenWeatherMapResponse.class)
                .map(this::mapToWeatherResponse)
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.warn("City {} not found in weather API", city);
                    return Mono.error(new WeatherNotFoundException("Weather data not available for city " + city));
                })
                .doOnError(ex -> log.error("Error fetching weather for city {}", city, ex));
    }
    private WeatherResponse mapToWeatherResponse(OpenWeatherMapResponse apiResponse) {
        return WeatherResponse.builder()
                .city(apiResponse.getName())
                .temperature(apiResponse.getMain().getTemp())
                .description(apiResponse.getWeather().get(0).getDescription())
                .humidity(apiResponse.getMain().getHumidity())
                .windSpeed(apiResponse.getWind().getSpeed())
                .pressure(apiResponse.getMain().getPressure())
                .icon(apiResponse.getWeather().get(0).getIcon())
                .build();
    }


}
