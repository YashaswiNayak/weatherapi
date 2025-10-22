package com.yashaswi.weatherapi.exception;

import com.yashaswi.weatherapi.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;

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
        ResponseEntity<ErrorResponse> resp = handler.handleWeatherNotFound(ex, request);

        assertThat(resp.getStatusCode().value()).isEqualTo(404);
        Assertions.assertNotNull(resp.getBody());
        assertThat(resp.getBody().getError()).isEqualTo("Weather Not Found");
        assertThat(resp.getBody().getMessage()).isEqualTo("City not found");
    }

    @Test
    void handleValidationError_returns400() {
        jakarta.validation.ConstraintViolationException ex =
                new jakarta.validation.ConstraintViolationException("Invalid parameter", null);
        ResponseEntity<ErrorResponse> resp = handler.handleValidationError(ex, request);

        assertThat(resp.getStatusCode().value()).isEqualTo(400);
        Assertions.assertNotNull(resp.getBody());
        assertThat(resp.getBody().getError()).isEqualTo("Validation Error");
    }

    @Test
    void handleGenericError_returns500() {
        Exception ex = new Exception("Unexpected");
        ResponseEntity<ErrorResponse> resp = handler.handleGenericError(ex, request);

        assertThat(resp.getStatusCode().value()).isEqualTo(500);
        Assertions.assertNotNull(resp.getBody());
        assertThat(resp.getBody().getError()).isEqualTo("Internal Server Error");
        assertThat(resp.getBody().getMessage()).contains("An unexpected error occurred");
    }
}
