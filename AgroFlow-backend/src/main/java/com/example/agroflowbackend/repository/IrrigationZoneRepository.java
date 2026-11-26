package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.persistence.entity.IrrigationZoneEntity;
import com.example.agroflowbackend.persistence.mapper.IrrigationZoneMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
// Repository-ul acesta ascunde detaliile JPA si returneaza obiecte de domain catre service.
public class IrrigationZoneRepository {

    private final IrrigationZoneJpaRepository irrigationZoneJpaRepository;
    private final IrrigationZoneMapper irrigationZoneMapper;

    public IrrigationZoneRepository(
            IrrigationZoneJpaRepository irrigationZoneJpaRepository,
            IrrigationZoneMapper irrigationZoneMapper
    ) {
        this.irrigationZoneJpaRepository = irrigationZoneJpaRepository;
        this.irrigationZoneMapper = irrigationZoneMapper;
    }

    public IrrigationZone save(IrrigationZone irrigationZone) {
        IrrigationZoneEntity entity = irrigationZoneMapper.toEntity(irrigationZone);
        IrrigationZoneEntity savedEntity = irrigationZoneJpaRepository.save(entity);
        return irrigationZoneMapper.toDomain(savedEntity);
    }

    public List<IrrigationZone> findByFarmId(Long farmId) {
        return irrigationZoneJpaRepository.findByFarm_Id(farmId)
                .stream()
                .map(irrigationZoneMapper::toDomain)
                .toList();
    }
}
