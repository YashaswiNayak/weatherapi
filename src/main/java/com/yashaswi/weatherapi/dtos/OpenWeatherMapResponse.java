package com.yashaswi.weatherapi.dtos;

import lombok.Data;

import java.util.List;

@Data
public class OpenWeatherMapResponse {
    private List<Weather> weather;
    private Main main;
    private Wind wind;
    private String name;

    @Data
    public static class Weather {
        private String main;
        private String description;
        private String icon;
    }

    @Data
    public static class Main {
        private Double temp;
        private Integer humidity;
        private Integer pressure;
    }

    @Data
    public static class Wind {
        private Double speed;
    }
}
