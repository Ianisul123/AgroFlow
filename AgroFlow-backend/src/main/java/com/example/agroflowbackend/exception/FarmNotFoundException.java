package com.example.agroflowbackend.exception;

public class FarmNotFoundException extends RuntimeException {

    public FarmNotFoundException(String message) {
        super(message);
    }
}