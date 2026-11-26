package com.example.agroflowbackend.dto;

public class DeleteFarmResponse {

    private String message;

    public DeleteFarmResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public DeleteFarmResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
