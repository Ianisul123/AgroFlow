package com.example.agroflowbackend.persistence.mapper;

import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.persistence.entity.FarmEntity;
import com.example.agroflowbackend.persistence.entity.IrrigationZoneEntity;
import org.springframework.stereotype.Component;

@Component
// Mapper-ul transforma intre obiectul de domain si entitatea folosita de baza de date.
public class IrrigationZoneMapper {

    public IrrigationZone toDomain(IrrigationZoneEntity entity) {
        if (entity == null) {
            return null;
        }

        // API-ul are nevoie doar de id-ul fermei, nu de tot obiectul Farm.
        Long farmId = entity.getFarm() == null ? null : entity.getFarm().getId();
        return new IrrigationZone(
                entity.getId(),
                farmId,
                entity.getName(),
                entity.getCropType(),
                entity.getAreaSize(),
                entity.getMoistureThreshold()
        );
    }

    public IrrigationZoneEntity toEntity(IrrigationZone domain) {
        if (domain == null) {
            return null;
        }

        IrrigationZoneEntity entity = new IrrigationZoneEntity();
        // Este mai util pentru partea de update, mai putin pentru cea de create
        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        FarmEntity farmEntity = new FarmEntity();
        // Este suficient sa setez id-ul ca JPA sa creeze legatura cu ferma.
        farmEntity.setId(domain.getFarmId());
        entity.setFarm(farmEntity);
        entity.setName(domain.getName());
        entity.setCropType(domain.getCropType());
        entity.setAreaSize(domain.getAreaSize());
        entity.setMoistureThreshold(domain.getMoistureThreshold());

        return entity;
    }
}
