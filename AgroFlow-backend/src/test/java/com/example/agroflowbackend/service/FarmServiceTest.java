package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.domain.UserAccount;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.FarmResponse;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.repository.FarmRepository;
import com.example.agroflowbackend.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmServiceTest {

    private static final AuthenticatedUser USER = new AuthenticatedUser(10L, "Farmer One", "farmer@example.com", UserRole.USER);
    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(99L, "Admin", "admin@example.com", UserRole.ADMIN);

    @Mock
    private FarmRepository farmRepository;

    @Mock
    private UserAccountRepository userAccountRepository;

    @InjectMocks
    private FarmService farmService;

    @Test
    void createFarmWithValidInputShouldReturnSuccessResponse() {
        Farm farmToCreate = new Farm("North Field", "Eindhoven");

        Farm savedFarm = new Farm(1L, "North Field", "Eindhoven", USER.getId());
        when(farmRepository.save(any(Farm.class))).thenReturn(savedFarm);

        Farm response = farmService.createFarm(farmToCreate, USER);

        assertEquals(1L, response.getId());
        assertEquals("North Field", response.getName());
        assertEquals("Eindhoven", response.getLocation());
        assertEquals(USER.getId(), response.getOwnerId());
        verify(farmRepository).save(argThat(farm ->
                USER.getId().equals(farm.getOwnerId())
                        && "North Field".equals(farm.getName())
                        && "Eindhoven".equals(farm.getLocation())));
    }

    @Test
    void createFarmWithMissingNameShouldThrowIllegalArgumentException() {
        Farm farmToCreate = new Farm(" ", "Eindhoven");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> farmService.createFarm(farmToCreate, USER)
        );

        assertEquals("Farm name is required.", exception.getMessage());
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void adminShouldSeeAllFarms() {
        List<Farm> expectedFarms = List.of(
                new Farm(1L, "North Field", "Eindhoven", 10L),
                new Farm(2L, "South Field", "Tilburg", 20L)
        );
        when(farmRepository.findAll()).thenReturn(expectedFarms);

        List<Farm> farms = farmService.getVisibleFarms(ADMIN);

        assertEquals(2, farms.size());
        assertEquals(expectedFarms, farms);
        assertTrue(farms.stream().anyMatch(farm -> "North Field".equals(farm.getName())));
        verify(farmRepository).findAll();
    }

    @Test
    void normalUserShouldSeeOnlyOwnedFarms() {
        List<Farm> expectedFarms = List.of(
                new Farm(1L, "North Field", "Eindhoven", USER.getId())
        );
        when(farmRepository.findAllByOwnerId(USER.getId())).thenReturn(expectedFarms);

        List<Farm> farms = farmService.getVisibleFarms(USER);

        assertEquals(expectedFarms, farms);
        verify(farmRepository).findAllByOwnerId(USER.getId());
        verify(farmRepository, never()).findAll();
    }

    @Test
    void adminFarmResponsesShouldIncludeOwnerDetails() {
        List<Farm> farms = List.of(
                new Farm(1L, "North Field", "Eindhoven", USER.getId())
        );
        UserAccount owner = new UserAccount(USER.getId(), "Farmer One", "farmer@example.com", "hash", UserRole.USER);

        when(farmRepository.findAll()).thenReturn(farms);
        when(userAccountRepository.findById(USER.getId())).thenReturn(Optional.of(owner));

        List<FarmResponse> responses = farmService.getVisibleFarmResponses(ADMIN);

        assertEquals(1, responses.size());
        assertEquals("Farmer One", responses.getFirst().getOwnerName());
        assertEquals("farmer@example.com", responses.getFirst().getOwnerEmail());
        verify(userAccountRepository).findById(USER.getId());
    }

    @Test
    void normalUserFarmResponsesShouldNotExposeOwnerDetails() {
        List<Farm> expectedFarms = List.of(
                new Farm(1L, "North Field", "Eindhoven", USER.getId())
        );
        when(farmRepository.findAllByOwnerId(USER.getId())).thenReturn(expectedFarms);

        List<FarmResponse> responses = farmService.getVisibleFarmResponses(USER);

        assertEquals(1, responses.size());
        assertEquals("North Field", responses.getFirst().getName());
        assertNull(responses.getFirst().getOwnerName());
        assertNull(responses.getFirst().getOwnerEmail());
        verify(userAccountRepository, never()).findById(any());
    }

    @Test
    void getFarmByIdShouldReturnFarmWhenFarmExists() {
        Farm farm = new Farm(1L, "North Field", "Eindhoven", USER.getId());
        when(farmRepository.findByIdAndOwnerId(1L, USER.getId())).thenReturn(Optional.of(farm));

        Farm result = farmService.getFarmById(1L, USER);

        assertEquals(1L, result.getId());
        assertEquals("North Field", result.getName());
        assertEquals("Eindhoven", result.getLocation());
        verify(farmRepository).findByIdAndOwnerId(1L, USER.getId());
    }

    @Test
    void adminShouldGetAnyFarmById() {
        Farm farm = new Farm(1L, "North Field", "Eindhoven", USER.getId());
        when(farmRepository.findById(1L)).thenReturn(Optional.of(farm));

        Farm result = farmService.getFarmById(1L, ADMIN);

        assertEquals(1L, result.getId());
        verify(farmRepository).findById(1L);
    }

    @Test
    void getFarmByIdShouldThrowExceptionWhenFarmDoesNotExistOrIsNotOwnedByUser() {
        when(farmRepository.findByIdAndOwnerId(999L, USER.getId())).thenReturn(Optional.empty());

        FarmNotFoundException exception = assertThrows(
                FarmNotFoundException.class,
                () -> farmService.getFarmById(999L, USER)
        );

        assertEquals("Farm not found.", exception.getMessage());
        verify(farmRepository).findByIdAndOwnerId(999L, USER.getId());
    }

    @Test
    void updateFarmShouldReturnSuccessResponse() {
        Farm existingFarm = new Farm(1L, "Old Farm", "Eindhoven", USER.getId());
        Farm updatedFarm = new Farm(1L, "Updated Farm", "Amsterdam", USER.getId());
        Farm farmToUpdate = new Farm("Updated Farm", "Amsterdam");

        when(farmRepository.findByIdAndOwnerId(1L, USER.getId())).thenReturn(Optional.of(existingFarm));
        when(farmRepository.save(any(Farm.class))).thenReturn(updatedFarm);

        Farm response = farmService.updateFarm(1L, farmToUpdate, USER);

        assertEquals(1L, response.getId());
        assertEquals("Updated Farm", response.getName());
        assertEquals("Amsterdam", response.getLocation());
        verify(farmRepository).findByIdAndOwnerId(1L, USER.getId());
        verify(farmRepository).save(argThat(farm ->
                farm.getId().equals(1L)
                        && USER.getId().equals(farm.getOwnerId())
                        && "Updated Farm".equals(farm.getName())
                        && "Amsterdam".equals(farm.getLocation())));
    }

    @Test
    void updateFarmShouldThrowExceptionWhenFarmDoesNotExist() {
        Farm farmToUpdate = new Farm("Updated Farm", "Amsterdam");

        when(farmRepository.findByIdAndOwnerId(999L, USER.getId())).thenReturn(Optional.empty());

        FarmNotFoundException exception = assertThrows(
                FarmNotFoundException.class,
                () -> farmService.updateFarm(999L, farmToUpdate, USER)
        );

        assertEquals("Farm not found.", exception.getMessage());
        verify(farmRepository).findByIdAndOwnerId(999L, USER.getId());
        verify(farmRepository, never()).save(any(Farm.class));
    }

    @Test
    void deleteFarmShouldReturnSuccessResponse() {
        Farm existingFarm = new Farm(1L, "North Field", "Eindhoven", USER.getId());
        when(farmRepository.findById(1L)).thenReturn(Optional.of(existingFarm));

        Farm deletedFarm = farmService.deleteFarm(1L, ADMIN);

        assertEquals(existingFarm, deletedFarm);
        verify(farmRepository).findById(1L);
        verify(farmRepository).delete(existingFarm);
    }

    @Test
    void deleteFarmShouldThrowExceptionWhenFarmDoesNotExist() {
        when(farmRepository.findById(999L)).thenReturn(Optional.empty());

        FarmNotFoundException exception = assertThrows(
                FarmNotFoundException.class,
                () -> farmService.deleteFarm(999L, ADMIN)
        );

        assertEquals("Farm not found.", exception.getMessage());
        verify(farmRepository).findById(999L);
        verify(farmRepository, never()).delete(any(Farm.class));
    }
}
