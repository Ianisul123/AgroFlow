package com.example.agroflowbackend.config;

import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.repository.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
//creează automat un admin default la pornire dacă nu există.
@Component
public class AdminUserInitializer implements ApplicationRunner {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminName;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserInitializer(
            UserAccountRepository userAccountRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.name:AgroFlow Admin}") String adminName,
            @Value("${app.admin.email:admin@agroflow.local}") String adminEmail,
@Value("${app.admin.password}") String adminPassword    ) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminName = adminName;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        String normalizedEmail = adminEmail.trim().toLowerCase();

        if (userAccountRepository.existsByEmail(normalizedEmail)) {
            return;
        }

        userAccountRepository.save(new UserAccount(
                adminName.trim(),
                normalizedEmail,
                passwordEncoder.encode(adminPassword),
                UserRole.ADMIN
        ));
    }
}
