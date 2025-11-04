package com.yashaswi.weatherapi.exception;

import com.yashaswi.weatherapi.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void init() {
        handler = new GlobalExceptionHandler();
        request = Mockito.mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/weather");
    }

    @Test
    void handleWeatherNotFound_returns404() {
        WeatherNotFoundException ex = new WeatherNotFoundException("City not found");
        ResponseEntity<ErrorResponse> response = handler.handleWeatherNotFound(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Weather Not Found");
        assertThat(response.getBody().getMessage()).isEqualTo("City not found");
    }

    @Test
    void handleValidationError_returns400() {
        ConstraintViolationException ex =
                new ConstraintViolationException("Validation error", Collections.emptySet());
        ResponseEntity<ErrorResponse> response = handler.handleValidationError(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Validation Error");
    }

    @Test
    void handleGenericError_returns500() {
        Exception ex = new Exception("Unexpected error");
        ResponseEntity<ErrorResponse> response = handler.handleGenericError(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Internal Server Error");
    }

    @Test
    void handleExternalApiException_returns502() {
        ExternalApiException ex = new ExternalApiException("Upstream API error (500): Service unavailable");
        ResponseEntity<ErrorResponse> response = handler.handleExternalApiError(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(502);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("EXTERNAL_API_ERROR");
        assertThat(response.getBody().getMessage()).contains("Upstream API error");
    }

    @Test
    void handleRedisConnectionException_returns503() {
        CacheUnavailableException ex = new CacheUnavailableException("Cache unavailable");
        ResponseEntity<ErrorResponse> response = handler.handleCacheUnavailable(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(503);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("CACHE_UNAVAILABLE");
        assertThat(response.getBody().getMessage()).contains("Cache is unavailable");
    }

    @Test
    void handleMissingParameter_returns400() {
        MissingServletRequestParameterException ex =
                new MissingServletRequestParameterException("city", "String");
        ResponseEntity<ErrorResponse> response = handler.handleMissingParameter(ex, request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Missing Parameter");
        assertThat(response.getBody().getMessage()).contains("city");
    }

    @Test
    void errorResponseShouldNotExposeStackTrace() {
        Exception ex = new Exception("Internal error with sensitive data");
        ResponseEntity<ErrorResponse> response = handler.handleGenericError(ex, request);

        // Verify no stack trace in message
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
        assertThat(response.getBody().getMessage()).doesNotContain("Exception");
        assertThat(response.getBody().getMessage()).doesNotContain("at com.yashaswi");
    }

}
