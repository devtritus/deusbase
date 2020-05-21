package com.devtritus.deusbase.api;

public class UnhandledCommandException extends RuntimeException {
    public UnhandledCommandException(String message) {
        super(message);
    }
}
