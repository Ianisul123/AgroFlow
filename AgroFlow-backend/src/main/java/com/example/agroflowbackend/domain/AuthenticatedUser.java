package com.example.agroflowbackend.domain;

import java.security.Principal;

public class AuthenticatedUser implements Principal {

    private final Long id;
    private final String name;
    private final String email;
    private final UserRole role;

    public AuthenticatedUser(Long id, String name, String email, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String getName() {
        return name;
    }
}
