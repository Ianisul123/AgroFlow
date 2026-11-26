package com.example.agroflowbackend.dto;

// DTO-ul trimite inapoi catre frontend id-ul zonei create si un mesaj de confirmare.
public class CreateIrrigationZoneResponse {
    private Long irrigationZoneId;
    private String message;

    public CreateIrrigationZoneResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public CreateIrrigationZoneResponse(Long irrigationZoneId, String message) {
        this.irrigationZoneId = irrigationZoneId;
        this.message = message;
    }

    public Long getIrrigationZoneId() {
        return irrigationZoneId;
    }

    public String getMessage() {
        return message;
    }

    public void setIrrigationZoneId(Long irrigationZoneId) {
        this.irrigationZoneId = irrigationZoneId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
