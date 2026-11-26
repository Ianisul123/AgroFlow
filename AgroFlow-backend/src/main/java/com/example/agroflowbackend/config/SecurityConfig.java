package com.example.agroflowbackend.config;

import com.example.agroflowbackend.security.BearerTokenAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.BeanCreationException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//configurează securitatea, endpoint-urile publice/private și rolurile.
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter
    ) {
        try {
            return http
                    // CSRF is disabled because this API is stateless and uses Bearer tokens instead of session cookies.
                    .csrf(AbstractHttpConfigurer::disable)
                    .cors(Customizer.withDefaults())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth
                            .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                            .requestMatchers(HttpMethod.POST, "/auth/register", "/auth/login").permitAll()
                            .requestMatchers("/ws").permitAll()
                            .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                            .requestMatchers(HttpMethod.DELETE, "/farms/**").hasRole("ADMIN")
                            .anyRequest().authenticated()
                    )
                    .exceptionHandling(exceptions -> exceptions
                            .authenticationEntryPoint((request, response, exception) -> writeSecurityError(
                                    response,
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Authentication is required."
                            ))
                            .accessDeniedHandler((request, response, exception) -> writeSecurityError(
                                    response,
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "Access denied."
                            ))
                    )
                    .addFilterBefore(bearerTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                    .build();
        } catch (Exception exception) {
            throw new BeanCreationException("securityFilterChain", "Failed to configure Spring Security.", exception);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private static void writeSecurityError(HttpServletResponse response, int status, String message) throws java.io.IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("{\"status\":" + status + ",\"message\":\"" + message + "\",\"errors\":null}");
    }
}
