package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.persistence.entity.IrrigationZoneEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository-ul JPA comunica direct cu baza de date pentru entitatea IrrigationZoneEntity.
public interface IrrigationZoneJpaRepository extends JpaRepository<IrrigationZoneEntity, Long> {

    // Spring Data foloseste numele metodei ca sa caute dupa id-ul din relatia cu ferma.
    List<IrrigationZoneEntity> findByFarm_Id(Long farmId);
}
