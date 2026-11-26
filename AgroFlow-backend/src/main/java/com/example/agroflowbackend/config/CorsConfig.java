package com.example.agroflowbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//permite frontend-ului să comunice cu backend-ul.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    // Originea frontend-ului care are voie sa apeleze API-ul REST.
    // Pentru demo local este http://localhost:5173, dar poate fi schimbata din
    // application.properties fara sa modificam codul Java.
    private final String allowedOrigin;

    public CorsConfig(@Value("${app.cors.allowed-origin:http://localhost:5173}") String allowedOrigin) {
        this.allowedOrigin = allowedOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // CORS aici se aplica request-urilor HTTP normale catre controllerele REST.
        // browserul blocheaza implicit request-uri intre
        // origin-uri diferite, iar aceasta configurare spune explicit ca frontend-ul
        // nostru are voie sa comunice cu backend-ul.
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigin)
                // Metodele HTTP folosite de aplicatie pentru operatii CRUD.
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
