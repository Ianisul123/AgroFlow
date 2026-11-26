package com.example.agroflowbackend.repository;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.persistence.entity.FarmEntity;
import com.example.agroflowbackend.persistence.mapper.FarmMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FarmRepository {

    private final FarmJpaRepository farmJpaRepository;
    private final FarmMapper farmMapper;

    public FarmRepository(FarmJpaRepository farmJpaRepository, FarmMapper farmMapper) {
        this.farmJpaRepository = farmJpaRepository;
        this.farmMapper = farmMapper;
    }

    public Farm save(Farm farm) {
        FarmEntity entity = farmMapper.toEntity(farm);
        FarmEntity savedEntity = farmJpaRepository.save(entity);
        return farmMapper.toDomain(savedEntity);
    }

    public List<Farm> findAll() {
        return farmJpaRepository.findAll()
                .stream()
                .map(farmMapper::toDomain)
                .toList();
    }

    public List<Farm> findAllByOwnerId(Long ownerId) {
        return farmJpaRepository.findByOwnerId(ownerId)
                .stream()
                .map(farmMapper::toDomain)
                .toList();
    }

    public Optional<Farm> findById(Long id) {
        return farmJpaRepository.findById(id)
                .map(farmMapper::toDomain);
    }

    public Optional<Farm> findByIdAndOwnerId(Long id, Long ownerId) {
        return farmJpaRepository.findByIdAndOwnerId(id, ownerId)
                .map(farmMapper::toDomain);
    }

    public boolean existsById(Long id) {
        return farmJpaRepository.existsById(id);
    }

    public void delete(Farm farm) {
        FarmEntity entity = farmMapper.toEntity(farm);
        farmJpaRepository.delete(entity);
    }
}
