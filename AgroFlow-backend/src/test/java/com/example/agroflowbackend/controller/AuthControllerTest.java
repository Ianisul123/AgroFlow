package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.AuthResponse;
import com.example.agroflowbackend.dto.AuthenticatedUserResponse;
import com.example.agroflowbackend.exception.GlobalExceptionHandler;
import com.example.agroflowbackend.exception.InvalidCredentialsException;
import com.example.agroflowbackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerTest {

    private MockMvc mockMvc;
    private AuthService authService;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        authService = mock(AuthService.class);
        authController = new AuthController(authService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void registerShouldReturnTokenAndUserRole() throws Exception {
        when(authService.register(any())).thenReturn(
                new AuthResponse("token-123", "Farmer One", "farmer@example.com", UserRole.USER, "Account was successfully created.")
        );

        String requestBody = """
                {
                  "name": "Farmer One",
                  "email": "farmer@example.com",
                  "password": "Secret123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-123"))
                .andExpect(jsonPath("$.name").value("Farmer One"))
                .andExpect(jsonPath("$.email").value("farmer@example.com"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.message").value("Account was successfully created."));
    }

    @Test
    void registerShouldReturnValidationErrorWhenPasswordDoesNotMeetRequirements() throws Exception {
        String requestBody = """
                {
                  "name": "Farmer One",
                  "email": "farmer@example.com",
                  "password": "secret123"
                }
                """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void loginShouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException("Invalid email or password."));

        String requestBody = """
                {
                  "email": "farmer@example.com",
                  "password": "Wrong123"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Invalid email or password."));
    }

    @Test
    void loginShouldReturnValidationErrorWhenPasswordIsMissing() throws Exception {
        String requestBody = """
                {
                  "email": "farmer@example.com"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void meShouldReturnAuthenticatedUserDetails() {
        AuthenticatedUser user = new AuthenticatedUser(1L, "Farmer One", "farmer@example.com", UserRole.USER);
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(user, null);

        AuthenticatedUserResponse response = authController.me(authentication);

        assertEquals("Farmer One", response.getName());
        assertEquals("farmer@example.com", response.getEmail());
        assertEquals(UserRole.USER, response.getRole());
    }

    @Test
    void meShouldRejectMissingAuthentication() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.me(null)
        );

        assertEquals(401, exception.getStatusCode().value());
    }

    @Test
    void meShouldRejectUnexpectedPrincipalType() {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("anonymous", null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> authController.me(authentication)
        );

        assertEquals(401, exception.getStatusCode().value());
    }
}
