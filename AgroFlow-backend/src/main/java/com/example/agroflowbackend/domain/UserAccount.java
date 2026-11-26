package com.example.agroflowbackend.domain;

public class UserAccount {

    private final Long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final UserRole role;

    public UserAccount(Long id, String name, String email, String passwordHash, UserRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public UserAccount(String name, String email, String passwordHash, UserRole role) {
        this(null, name, email, passwordHash, role);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }
}
