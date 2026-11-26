package com.example.agroflowbackend.dto;

public class FarmResponse {

    private Long id;
    private String name;
    private String location;
    private Long ownerId;
    private String ownerName;
    private String ownerEmail;

    public FarmResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public FarmResponse(Long id, String name, String location, Long ownerId, String ownerName, String ownerEmail) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerEmail = ownerEmail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLocation() {
        return location;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }
}
