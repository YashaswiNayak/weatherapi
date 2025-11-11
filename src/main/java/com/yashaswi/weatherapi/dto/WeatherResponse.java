package com.yashaswi.weatherapi.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String city;
    private String country;
    private Double temperature;
    private String description;
    private Integer humidity;
    private Double windSpeed;
    private Integer pressure;
    private String icon;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Builder.Default
    private String source="weatherapi";
}
