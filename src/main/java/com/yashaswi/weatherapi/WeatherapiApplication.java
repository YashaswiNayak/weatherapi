package com.yashaswi.weatherapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WeatherapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeatherapiApplication.class, args);
        System.out.println("🌤️  Weather API Service is running on http://localhost:8080");
        System.out.println("📋 Available endpoints:");
        System.out.println("   GET /api/weather?city=london");
        System.out.println("   GET /api/weather/cities");
        System.out.println("   GET /api/weather/health");
        System.out.println("   GET /actuator/health");
    }

}
