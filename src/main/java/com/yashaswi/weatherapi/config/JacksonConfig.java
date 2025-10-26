package com.yashaswi.weatherapi.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        // Create a new mapper and register the JavaTimeModule so LocalDateTime is supported
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        // Optional: Disable timestamps (so you get ISO 8601 date strings)
        mapper.findAndRegisterModules();
        return mapper;
    }
}
