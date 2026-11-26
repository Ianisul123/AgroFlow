package com.example.agroflowbackend.dto;

public class LogoutResponse {

    private String message;

    public LogoutResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public LogoutResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
