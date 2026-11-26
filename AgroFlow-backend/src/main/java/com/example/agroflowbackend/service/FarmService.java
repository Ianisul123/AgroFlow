package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.FarmResponse;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.repository.FarmRepository;
import com.example.agroflowbackend.repository.UserAccountRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FarmService {

    private static final String FARM_NOT_FOUND_MESSAGE = "Farm not found.";

    private final FarmRepository farmRepository;
    private final UserAccountRepository userAccountRepository;

    public FarmService(FarmRepository farmRepository, UserAccountRepository userAccountRepository) {
        this.farmRepository = farmRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public Farm createFarm(Farm farm, AuthenticatedUser currentUser) {
        validateAuthenticatedUser(currentUser);
        if (farm == null || farm.getName() == null || farm.getName().isBlank()) {
            throw new IllegalArgumentException("Farm name is required.");
        }

        Farm farmToCreate = new Farm(farm.getName(), farm.getLocation(), currentUser.getId());
        return farmRepository.save(farmToCreate);
    }

    public List<Farm> getVisibleFarms(AuthenticatedUser currentUser) {
        validateAuthenticatedUser(currentUser);

        if (isAdmin(currentUser)) {
            return farmRepository.findAll();
        }

        return farmRepository.findAllByOwnerId(currentUser.getId());
    }

    public List<FarmResponse> getVisibleFarmResponses(AuthenticatedUser currentUser) {
        return getVisibleFarms(currentUser)
                .stream()
                .map(farm -> toFarmResponse(farm, currentUser))
                .toList();
    }

    public Farm getFarmById(Long id, AuthenticatedUser currentUser) {
        return findVisibleFarmById(id, currentUser);
    }

    public FarmResponse getFarmResponseById(Long id, AuthenticatedUser currentUser) {
        return toFarmResponse(getFarmById(id, currentUser), currentUser);
    }

    public Farm updateFarm(Long id, Farm farmData, AuthenticatedUser currentUser) {
        Farm farm = findVisibleFarmById(id, currentUser);

        farm.updateInformation(farmData.getName(), farmData.getLocation());

        return farmRepository.save(farm);
    }

    public Farm deleteFarm(Long id, AuthenticatedUser currentUser) {
        Farm farm = findVisibleFarmById(id, currentUser);

        farmRepository.delete(farm);
        return farm;
    }

    private Farm findVisibleFarmById(Long id, AuthenticatedUser currentUser) {
        validateAuthenticatedUser(currentUser);

        if (isAdmin(currentUser)) {
            return farmRepository.findById(id)
                    .orElseThrow(() -> new FarmNotFoundException(FARM_NOT_FOUND_MESSAGE));
        }

        return farmRepository.findByIdAndOwnerId(id, currentUser.getId())
                .orElseThrow(() -> new FarmNotFoundException(FARM_NOT_FOUND_MESSAGE));
    }

    private boolean isAdmin(AuthenticatedUser currentUser) {
        return UserRole.ADMIN.equals(currentUser.getRole());
    }

    private FarmResponse toFarmResponse(Farm farm, AuthenticatedUser currentUser) {
        if (!isAdmin(currentUser)) {
            return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(), null, null, null);
        }

        if (farm.getOwnerId() == null) {
            return new FarmResponse(farm.getId(), farm.getName(), farm.getLocation(), null, "Unassigned", null);
        }

        return userAccountRepository.findById(farm.getOwnerId())
                .map(owner -> toFarmResponse(farm, owner))
                .orElseGet(() -> new FarmResponse(
                        farm.getId(),
                        farm.getName(),
                        farm.getLocation(),
                        farm.getOwnerId(),
                        "Unknown user",
                        null
                ));
    }

    private FarmResponse toFarmResponse(Farm farm, UserAccount owner) {
        return new FarmResponse(
                farm.getId(),
                farm.getName(),
                farm.getLocation(),
                owner.getId(),
                owner.getName(),
                owner.getEmail()
        );
    }

    private void validateAuthenticatedUser(AuthenticatedUser currentUser) {
        if (currentUser == null || currentUser.getId() == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
    }
}
