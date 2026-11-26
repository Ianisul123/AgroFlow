package com.example.agroflowbackend.security;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.service.AuthTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BearerTokenAuthenticationFilterTest {

    @Test
    void validAdminTokenShouldCreateAdminAuthentication() throws Exception {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        when(authTokenService.extractToken("Bearer admin-token")).thenReturn(Optional.of("admin-token"));
        when(authTokenService.findByToken("admin-token")).thenReturn(Optional.of(
                new AuthenticatedUser(1L, "AgroFlow Admin", "admin@agroflow.local", UserRole.ADMIN)
        ));

        BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter(authTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<Authentication> authenticationInChain = new AtomicReference<>();
        request.addHeader("Authorization", "Bearer admin-token");

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                authenticationInChain.set(SecurityContextHolder.getContext().getAuthentication())
        );

        Authentication authentication = authenticationInChain.get();
        assertEquals("AgroFlow Admin", authentication.getName());
        assertTrue(authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority())));
    }

    @Test
    void invalidTokenShouldReturnUnauthorized() throws Exception {
        AuthTokenService authTokenService = mock(AuthTokenService.class);
        when(authTokenService.extractToken("Bearer invalid-token")).thenReturn(Optional.of("invalid-token"));
        when(authTokenService.findByToken("invalid-token")).thenReturn(Optional.empty());

        BearerTokenAuthenticationFilter filter = new BearerTokenAuthenticationFilter(authTokenService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.addHeader("Authorization", "Bearer invalid-token");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
        });

        assertEquals(401, response.getStatus());
    }
}
