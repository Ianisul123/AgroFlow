package com.example.agroflowbackend.domain;

public class Farm {

    private final Long id;
    private final Long ownerId;
    private String name;
    private String location;

    public Farm(Long id, String name, String location) {
        this(id, name, location, null);
    }

    public Farm(Long id, String name, String location, Long ownerId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.ownerId = ownerId;
    }

    public Farm(String name, String location) {
        this(null, name, location);
    }

    public Farm(String name, String location, Long ownerId) {
        this(null, name, location, ownerId);
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

    public void updateInformation(String name, String location) {
        this.name = name;
        this.location = location;
    }
}
