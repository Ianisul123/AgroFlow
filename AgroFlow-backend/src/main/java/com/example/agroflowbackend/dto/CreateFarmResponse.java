package com.example.agroflowbackend.dto;

public class CreateFarmResponse {
    private Long farmId;
    private String message;

    public CreateFarmResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public CreateFarmResponse(Long farmId, String message) {
        this.farmId = farmId;
        this.message = message;
    }

    public Long getFarmId() {
        return farmId;
    }

    public String getMessage() {
        return message;
    }

    public void setFarmId(Long farmId) {
        this.farmId = farmId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
