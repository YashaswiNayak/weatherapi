package com.yashaswi.weatherapi.service;

import com.yashaswi.weatherapi.config.WeatherApiProperties;
import com.yashaswi.weatherapi.dto.OpenWeatherMapResponse;
import com.yashaswi.weatherapi.dto.WeatherResponse;
import com.yashaswi.weatherapi.exception.ExternalApiException;
import com.yashaswi.weatherapi.exception.CacheUnavailableException;
import com.yashaswi.weatherapi.exception.WeatherNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.RedisConnectionFailureException;
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

    @Cacheable(value = "weather", key = "#city.toLowerCase()", unless = "#result == null")
    public Mono<WeatherResponse> getWeatherByCity(String city) {
        String uri = String.format("/weather?q=%s&appid=%s&units=metric", city, weatherApiProperties.getApiKey());

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(OpenWeatherMapResponse.class)
                .map(this::mapToWeatherResponse)
                .onErrorResume(WebClientResponseException.NotFound.class, ex -> {
                    log.warn("City {} not found in weather API", city);
                    return Mono.error(new WeatherNotFoundException("Weather data not available for city " + city));
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    log.warn("OpenWeatherMap API error for city {}: {}", city, ex.getStatusCode());
                    return Mono.error(new ExternalApiException("Upstream weather API error (" + ex.getStatusCode().value() + ") " + ex.getResponseBodyAsString()));
                })
                .onErrorResume(RedisConnectionFailureException.class, ex -> {
                    log.error("Redis unavailable during fetch for city '{}'", city, ex);
                    return Mono.error(new CacheUnavailableException("Caching currently unavailable. Please try again later."));
                })
                .doOnError(ex -> log.error("Error fetching weather for city {}", city, ex));
    }

    private WeatherResponse mapToWeatherResponse(OpenWeatherMapResponse apiResponse) {
        return WeatherResponse.builder()
                .city(apiResponse.getName())
                .temperature(apiResponse.getMain().getTemp())
                .description(apiResponse.getWeather().getFirst().getDescription())
                .humidity(apiResponse.getMain().getHumidity())
                .windSpeed(apiResponse.getWind().getSpeed())
                .pressure(apiResponse.getMain().getPressure())
                .icon(apiResponse.getWeather().getFirst().getIcon())
                .build();
    }


}
