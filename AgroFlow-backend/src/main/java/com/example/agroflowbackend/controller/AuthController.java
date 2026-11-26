package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.dto.AuthResponse;
import com.example.agroflowbackend.dto.AuthenticatedUserResponse;
import com.example.agroflowbackend.dto.LoginRequest;
import com.example.agroflowbackend.dto.LogoutResponse;
import com.example.agroflowbackend.dto.RegisterRequest;
import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public LogoutResponse logout(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader) {
        authService.logout(authorizationHeader);
        return new LogoutResponse("Logout successful.");
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return new AuthenticatedUserResponse(user.getName(), user.getEmail(), user.getRole());
    }
}
