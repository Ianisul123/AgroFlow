package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserAccount;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class AuthTokenService {

    private static final String BEARER_PREFIX = "Bearer ";

    private final ConcurrentMap<String, AuthenticatedUser> activeTokens = new ConcurrentHashMap<>();

    public String createToken(UserAccount userAccount) {
        String token = UUID.randomUUID().toString();
        activeTokens.put(
                token,
                new AuthenticatedUser(
                        userAccount.getId(),
                        userAccount.getName(),
                        userAccount.getEmail(),
                        userAccount.getRole()
                )
        );
        return token;
    }

    public Optional<AuthenticatedUser> findByToken(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        return Optional.ofNullable(activeTokens.get(token));
    }

    public void revokeToken(String token) {
        if (token != null) {
            activeTokens.remove(token);
        }
    }

    public Optional<String> extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }

        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(token);
    }
}
