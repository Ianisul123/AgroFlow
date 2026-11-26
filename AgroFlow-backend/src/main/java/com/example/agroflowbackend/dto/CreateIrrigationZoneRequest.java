package com.example.agroflowbackend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

// DTO-ul contine datele primite de la frontend cand se creeaza o zona noua.
public class CreateIrrigationZoneRequest {

    @NotBlank(message = "Zone name is required.")
    @Size(min = 1, max = 100, message = "Zone name must be between 1 and 100 characters.")
    private String name;

    @NotBlank(message = "Crop type is required.")
    @Size(min = 1, max = 100, message = "Crop type must be between 1 and 100 characters.")
    private String cropType;

    @NotNull(message = "Area size is required.")
    @Positive(message = "Area size must be greater than 0.")
    private Double areaSize;

    @NotNull(message = "Moisture threshold is required.")
    @DecimalMin(value = "0.0", message = "Moisture threshold must be at least 0.")
    @DecimalMax(value = "100.0", message = "Moisture threshold must be at most 100.")
    private Double moistureThreshold;

    public CreateIrrigationZoneRequest() {
        // Required by Jackson for request body deserialization.
    }

    public String getName() {
        return name;
    }

    public String getCropType() {
        return cropType;
    }

    public Double getAreaSize() {
        return areaSize;
    }

    public Double getMoistureThreshold() {
        return moistureThreshold;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCropType(String cropType) {
        this.cropType = cropType;
    }

    public void setAreaSize(Double areaSize) {
        this.areaSize = areaSize;
    }

    public void setMoistureThreshold(Double moistureThreshold) {
        this.moistureThreshold = moistureThreshold;
    }
}
