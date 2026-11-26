package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthTokenServiceTest {

    @Test
    void createTokenShouldStoreAuthenticatedUserWithRole() {
        AuthTokenService authTokenService = new AuthTokenService();
        UserAccount userAccount = new UserAccount(1L, "Farmer One", "farmer@example.com", "hash", UserRole.ADMIN);

        String token = authTokenService.createToken(userAccount);
        Optional<AuthenticatedUser> user = authTokenService.findByToken(token);

        assertNotNull(token);
        assertTrue(user.isPresent());
        assertEquals(1L, user.get().getId());
        assertEquals("farmer@example.com", user.get().getEmail());
        assertEquals("Farmer One", user.get().getName());
        assertEquals(UserRole.ADMIN, user.get().getRole());
    }

    @Test
    void revokeTokenShouldRemoveToken() {
        AuthTokenService authTokenService = new AuthTokenService();
        String token = authTokenService.createToken(new UserAccount(1L, "Farmer One", "farmer@example.com", "hash", UserRole.USER));

        authTokenService.revokeToken(token);

        assertTrue(authTokenService.findByToken(token).isEmpty());
    }

    @Test
    void extractTokenShouldReturnBearerValue() {
        AuthTokenService authTokenService = new AuthTokenService();

        Optional<String> token = authTokenService.extractToken("Bearer abc-123");

        assertTrue(token.isPresent());
        assertEquals("abc-123", token.get());
    }
}
