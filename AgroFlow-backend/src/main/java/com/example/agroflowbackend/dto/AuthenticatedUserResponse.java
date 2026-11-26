package com.example.agroflowbackend.dto;

import com.example.agroflowbackend.domain.UserRole;

public class AuthenticatedUserResponse {

    private String name;
    private String email;
    private UserRole role;

    public AuthenticatedUserResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public AuthenticatedUserResponse(String name, String email, UserRole role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
