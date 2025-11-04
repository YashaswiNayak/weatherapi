package com.yashaswi.weatherapi.exception;

public class CacheUnavailableException extends RuntimeException {
    public CacheUnavailableException(String message) {
        super(message);
    }

    public CacheUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
