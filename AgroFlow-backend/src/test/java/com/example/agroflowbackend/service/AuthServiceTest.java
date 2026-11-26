package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.AuthResponse;
import com.example.agroflowbackend.dto.LoginRequest;
import com.example.agroflowbackend.dto.RegisterRequest;
import com.example.agroflowbackend.exception.InvalidCredentialsException;
import com.example.agroflowbackend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthTokenService authTokenService;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerShouldHashPasswordAndCreateNormalUser() {
        RegisterRequest request = new RegisterRequest();
        request.setName(" Farmer One ");
        request.setEmail(" FARMER@EXAMPLE.COM ");
        request.setPassword("Secret123");

        when(userAccountRepository.existsByEmail("farmer@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret123")).thenReturn("hashed-password");
        when(userAccountRepository.save(any(UserAccount.class)))
                .thenReturn(new UserAccount(1L, "Farmer One", "farmer@example.com", "hashed-password", UserRole.USER));
        when(authTokenService.createToken(any(UserAccount.class))).thenReturn("token-123");

        AuthResponse response = authService.register(request);

        assertEquals("token-123", response.getToken());
        assertEquals("Farmer One", response.getName());
        assertEquals("farmer@example.com", response.getEmail());
        assertEquals(UserRole.USER, response.getRole());
        assertEquals("Account was successfully created.", response.getMessage());

        ArgumentCaptor<UserAccount> userCaptor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(userCaptor.capture());
        assertEquals("Farmer One", userCaptor.getValue().getName());
        assertEquals("farmer@example.com", userCaptor.getValue().getEmail());
        assertEquals("hashed-password", userCaptor.getValue().getPasswordHash());
        assertEquals(UserRole.USER, userCaptor.getValue().getRole());
        assertNotEquals("Secret123", userCaptor.getValue().getPasswordHash());
    }

    @Test
    void registerShouldRejectDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Farmer One");
        request.setEmail("farmer@example.com");
        request.setPassword("Secret123");

        when(userAccountRepository.existsByEmail("farmer@example.com")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.register(request)
        );

        assertEquals("Email is already registered.", exception.getMessage());
        verify(userAccountRepository, never()).save(any(UserAccount.class));
    }

    @Test
    void loginShouldReturnTokenForValidCredentialsWithRole() {
        LoginRequest request = new LoginRequest();
        request.setEmail(" FARMER@EXAMPLE.COM ");
        request.setPassword("Secret123");

        UserAccount userAccount = new UserAccount(1L, "Farmer One", "farmer@example.com", "hashed-password", UserRole.ADMIN);
        when(userAccountRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("Secret123", "hashed-password")).thenReturn(true);
        when(authTokenService.createToken(userAccount)).thenReturn("token-123");

        AuthResponse response = authService.login(request);

        assertEquals("token-123", response.getToken());
        assertEquals("Farmer One", response.getName());
        assertEquals("farmer@example.com", response.getEmail());
        assertEquals(UserRole.ADMIN, response.getRole());
        assertEquals("Login successful.", response.getMessage());
    }

    @Test
    void loginShouldRejectInvalidPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("farmer@example.com");
        request.setPassword("Wrong123");

        UserAccount userAccount = new UserAccount(1L, "Farmer One", "farmer@example.com", "hashed-password", UserRole.USER);
        when(userAccountRepository.findByEmail("farmer@example.com")).thenReturn(Optional.of(userAccount));
        when(passwordEncoder.matches("Wrong123", "hashed-password")).thenReturn(false);

        InvalidCredentialsException exception = assertThrows(
                InvalidCredentialsException.class,
                () -> authService.login(request)
        );

        assertEquals("Invalid email or password.", exception.getMessage());
        verify(authTokenService, never()).createToken(any(UserAccount.class));
    }
}
