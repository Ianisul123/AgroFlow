package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.repository.FarmRepository;
import com.example.agroflowbackend.repository.IrrigationZoneRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
// Service-ul contine logica pentru creare, validare si listare a zonelor de irigare.
public class IrrigationZoneService {

    private final IrrigationZoneRepository irrigationZoneRepository;
    private final FarmRepository farmRepository;

    public IrrigationZoneService(IrrigationZoneRepository irrigationZoneRepository, FarmRepository farmRepository) {
        this.irrigationZoneRepository = irrigationZoneRepository;
        this.farmRepository = farmRepository;
    }

    public IrrigationZone createIrrigationZone(Long farmId, IrrigationZone irrigationZone, AuthenticatedUser currentUser) {
        // Zona nu trebuie salvata daca ferma nu exista.
        validateFarmAccess(farmId, currentUser);
        validateIrrigationZone(irrigationZone);

        IrrigationZone zoneToCreate = new IrrigationZone(
                farmId,
                irrigationZone.getName(),
                irrigationZone.getCropType(),
                irrigationZone.getAreaSize(),
                irrigationZone.getMoistureThreshold()
        );

        return irrigationZoneRepository.save(zoneToCreate);
    }

    public List<IrrigationZone> getIrrigationZonesByFarmId(Long farmId, AuthenticatedUser currentUser) {
        validateFarmAccess(farmId, currentUser);
        return irrigationZoneRepository.findByFarmId(farmId);
    }

    private void validateFarmAccess(Long farmId, AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }

        boolean farmIsVisible = UserRole.ADMIN.equals(currentUser.getRole())
                ? farmRepository.existsById(farmId)
                : farmRepository.findByIdAndOwnerId(farmId, currentUser.getId()).isPresent();

        if (!farmIsVisible) {
            throw new FarmNotFoundException("Farm not found.");
        }
    }

    private void validateIrrigationZone(IrrigationZone irrigationZone) {
        // Verificarile protejeaza service-ul chiar daca request-ul nu vine prin controller.
        if (irrigationZone == null || irrigationZone.getName() == null || irrigationZone.getName().isBlank()) {
            throw new IllegalArgumentException("Zone name is required.");
        }

        if (irrigationZone.getCropType() == null || irrigationZone.getCropType().isBlank()) {
            throw new IllegalArgumentException("Crop type is required.");
        }

        if (irrigationZone.getAreaSize() == null || irrigationZone.getAreaSize() <= 0) {
            throw new IllegalArgumentException("Area size must be greater than 0.");
        }

        if (irrigationZone.getMoistureThreshold() == null
                || irrigationZone.getMoistureThreshold() < 0
                || irrigationZone.getMoistureThreshold() > 100) {
            throw new IllegalArgumentException("Moisture threshold must be between 0 and 100.");
        }
    }
}
