package com.example.agroflowbackend.service;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.repository.FarmRepository;
import com.example.agroflowbackend.repository.IrrigationZoneRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IrrigationZoneServiceTest {

    private static final AuthenticatedUser USER = new AuthenticatedUser(10L, "Farmer One", "farmer@example.com", UserRole.USER);

    @Mock
    private IrrigationZoneRepository irrigationZoneRepository;

    @Mock
    private FarmRepository farmRepository;

    @InjectMocks
    private IrrigationZoneService irrigationZoneService;

    @Test
    void createIrrigationZoneWithValidInputShouldReturnCreatedZone() {
        IrrigationZone zoneToCreate = new IrrigationZone(1L, "Greenhouse A", "Tomatoes", 12.5, 35.0);
        IrrigationZone savedZone = new IrrigationZone(10L, 1L, "Greenhouse A", "Tomatoes", 12.5, 35.0);

        when(farmRepository.findByIdAndOwnerId(1L, USER.getId()))
                .thenReturn(Optional.of(new Farm(1L, "North Field", "Eindhoven", USER.getId())));
        when(irrigationZoneRepository.save(any(IrrigationZone.class))).thenReturn(savedZone);

        IrrigationZone response = irrigationZoneService.createIrrigationZone(1L, zoneToCreate, USER);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getFarmId());
        assertEquals("Greenhouse A", response.getName());
        assertEquals("Tomatoes", response.getCropType());
        assertEquals(12.5, response.getAreaSize());
        assertEquals(35.0, response.getMoistureThreshold());
        verify(irrigationZoneRepository).save(argThat(zone ->
                zone.getFarmId().equals(1L)
                        && "Greenhouse A".equals(zone.getName())
                        && "Tomatoes".equals(zone.getCropType())
                        && zone.getAreaSize().equals(12.5)
                        && zone.getMoistureThreshold().equals(35.0)));
    }

    @Test
    void createIrrigationZoneShouldThrowExceptionWhenFarmDoesNotExist() {
        IrrigationZone zoneToCreate = new IrrigationZone(999L, "Greenhouse A", "Tomatoes", 12.5, 35.0);
        when(farmRepository.findByIdAndOwnerId(999L, USER.getId())).thenReturn(Optional.empty());

        FarmNotFoundException exception = assertThrows(
                FarmNotFoundException.class,
                () -> irrigationZoneService.createIrrigationZone(999L, zoneToCreate, USER)
        );

        assertEquals("Farm not found.", exception.getMessage());
        verify(irrigationZoneRepository, never()).save(any(IrrigationZone.class));
    }

    @Test
    void createIrrigationZoneShouldValidateRequiredTextFields() {
        when(farmRepository.findByIdAndOwnerId(1L, USER.getId()))
                .thenReturn(Optional.of(new Farm(1L, "North Field", "Eindhoven", USER.getId())));
        IrrigationZone zoneToCreate = new IrrigationZone(1L, " ", "Tomatoes", 12.5, 35.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> irrigationZoneService.createIrrigationZone(1L, zoneToCreate, USER)
        );

        assertEquals("Zone name is required.", exception.getMessage());
        verify(irrigationZoneRepository, never()).save(any(IrrigationZone.class));
    }

    @Test
    void createIrrigationZoneShouldValidateAreaSize() {
        when(farmRepository.findByIdAndOwnerId(1L, USER.getId()))
                .thenReturn(Optional.of(new Farm(1L, "North Field", "Eindhoven", USER.getId())));
        IrrigationZone zoneToCreate = new IrrigationZone(1L, "Greenhouse A", "Tomatoes", 0.0, 35.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> irrigationZoneService.createIrrigationZone(1L, zoneToCreate, USER)
        );

        assertEquals("Area size must be greater than 0.", exception.getMessage());
        verify(irrigationZoneRepository, never()).save(any(IrrigationZone.class));
    }

    @Test
    void createIrrigationZoneShouldValidateMoistureThreshold() {
        when(farmRepository.findByIdAndOwnerId(1L, USER.getId()))
                .thenReturn(Optional.of(new Farm(1L, "North Field", "Eindhoven", USER.getId())));
        IrrigationZone zoneToCreate = new IrrigationZone(1L, "Greenhouse A", "Tomatoes", 12.5, 120.0);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> irrigationZoneService.createIrrigationZone(1L, zoneToCreate, USER)
        );

        assertEquals("Moisture threshold must be between 0 and 100.", exception.getMessage());
        verify(irrigationZoneRepository, never()).save(any(IrrigationZone.class));
    }

    @Test
    void getIrrigationZonesByFarmIdShouldReturnZonesForExistingFarm() {
        List<IrrigationZone> expectedZones = List.of(
                new IrrigationZone(10L, 1L, "Greenhouse A", "Tomatoes", 12.5, 35.0)
        );
        when(farmRepository.findByIdAndOwnerId(1L, USER.getId()))
                .thenReturn(Optional.of(new Farm(1L, "North Field", "Eindhoven", USER.getId())));
        when(irrigationZoneRepository.findByFarmId(1L)).thenReturn(expectedZones);

        List<IrrigationZone> zones = irrigationZoneService.getIrrigationZonesByFarmId(1L, USER);

        assertEquals(expectedZones, zones);
        verify(irrigationZoneRepository).findByFarmId(1L);
    }
}
