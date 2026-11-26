package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.persistence.entity.FarmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FarmJpaRepository extends JpaRepository<FarmEntity, Long> {

    List<FarmEntity> findByOwnerId(Long ownerId);

    Optional<FarmEntity> findByIdAndOwnerId(Long id, Long ownerId);
}
