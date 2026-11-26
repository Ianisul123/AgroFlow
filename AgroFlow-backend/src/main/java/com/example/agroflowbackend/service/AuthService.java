package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.AuthResponse;
import com.example.agroflowbackend.dto.LoginRequest;
import com.example.agroflowbackend.dto.RegisterRequest;
import com.example.agroflowbackend.exception.InvalidCredentialsException;
import com.example.agroflowbackend.repository.UserAccountRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthTokenService authTokenService;

    public AuthService(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            AuthTokenService authTokenService
    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authTokenService = authTokenService;
    }

    public AuthResponse register(RegisterRequest request) {
        String name = normalizeName(request.getName());
        String email = normalizeEmail(request.getEmail());

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        UserAccount savedUser = userAccountRepository.save(
                new UserAccount(name, email, passwordEncoder.encode(request.getPassword()), UserRole.USER)
        );
        String token = authTokenService.createToken(savedUser);

        return toAuthResponse(token, savedUser, "Account was successfully created.");
    }

    public AuthResponse login(LoginRequest request) {
        String email = normalizeEmail(request.getEmail());
        UserAccount userAccount = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password."));

        if (!passwordEncoder.matches(request.getPassword(), userAccount.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password.");
        }

        String token = authTokenService.createToken(userAccount);
        return toAuthResponse(token, userAccount, "Login successful.");
    }

    public void logout(String authorizationHeader) {
        authTokenService.extractToken(authorizationHeader)
                .ifPresent(authTokenService::revokeToken);
    }

    private AuthResponse toAuthResponse(String token, UserAccount userAccount, String message) {
        return new AuthResponse(
                token,
                userAccount.getName(),
                userAccount.getEmail(),
                userAccount.getRole(),
                message
        );
    }

    private String normalizeName(String name) {
        return name.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }
}
