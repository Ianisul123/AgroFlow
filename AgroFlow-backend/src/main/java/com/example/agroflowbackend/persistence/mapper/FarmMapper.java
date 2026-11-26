package com.example.agroflowbackend.persistence.mapper;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.persistence.entity.FarmEntity;
import org.springframework.stereotype.Component;

@Component
public class FarmMapper {

    public Farm toDomain(FarmEntity entity) {
        if (entity == null) {
            return null;
        }

        return new Farm(entity.getId(), entity.getName(), entity.getLocation(), entity.getOwnerId());
    }

    public FarmEntity toEntity(Farm domain) {
        if (domain == null) return null;

        FarmEntity entity = new FarmEntity();

        if (domain.getId() != null) {
            entity.setId(domain.getId());
        }

        entity.setName(domain.getName());
        entity.setLocation(domain.getLocation());
        entity.setOwnerId(domain.getOwnerId());

        return entity;
    }
}
