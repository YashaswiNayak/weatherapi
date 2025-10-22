package com.yashaswi.weatherapi.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class WeatherControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shallReturnWeatherForValidCity() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("city", "London"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.city").value("London"))
                .andExpect(jsonPath("$.description").exists());
    }

    @Test
    void shallReturnErrorForNonValidCity() throws Exception {
        mockMvc.perform(get("/api/weather")
                        .param("city", "unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Weather Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Weather data not available for city: unknown"));
    }

    @Test
    void shouldReturn400WhenCityParameterMissing() throws Exception {
        mockMvc.perform(get("/api/weather"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Missing Parameter"))
                .andExpect(jsonPath("$.status").value(400));
    }
}
