package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.persistence.entity.UserAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountJpaRepository extends JpaRepository<UserAccountEntity, Long> {

    Optional<UserAccountEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
