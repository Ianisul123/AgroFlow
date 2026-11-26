package com.example.agroflowbackend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateFarmRequest {

    @NotBlank(message = "Farm name is required.")
    @Size(min = 1, max = 100, message = "Farm name must be between 1 and 100 characters.")
    private String name;

    @Size(max = 150, message = "Location must be at most 150 characters.")
    private String location;

    public CreateFarmRequest() {
        // Required by Jackson for request body deserialization.
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
