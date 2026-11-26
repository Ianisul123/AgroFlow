package com.example.agroflowbackend.dto;

import com.example.agroflowbackend.domain.UserRole;

public class AuthResponse {

    private String token;
    private String name;
    private String email;
    private UserRole role;
    private String message;

    public AuthResponse() {
        // Required by Jackson for JSON serialization and deserialization.
    }

    public AuthResponse(String token, String name, String email, UserRole role, String message) {
        this.token = token;
        this.name = name;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public String getToken() {
        return token;
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

    public String getMessage() {
        return message;
    }

    public void setToken(String token) {
        this.token = token;
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

    public void setMessage(String message) {
        this.message = message;
    }
}
