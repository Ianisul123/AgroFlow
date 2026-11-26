package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.dto.CreateIrrigationZoneRequest;
import com.example.agroflowbackend.dto.CreateIrrigationZoneResponse;
import com.example.agroflowbackend.service.IrrigationZoneService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/farms/{farmId}/irrigation-zones")
// Controller-ul primeste request-urile pentru zonele de irigare si le trimite catre service.
public class IrrigationZoneController {

    private final IrrigationZoneService irrigationZoneService;

    public IrrigationZoneController(IrrigationZoneService irrigationZoneService) {
        this.irrigationZoneService = irrigationZoneService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateIrrigationZoneResponse createIrrigationZone(
            @PathVariable Long farmId,
            @Valid @RequestBody CreateIrrigationZoneRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser currentUser = getAuthenticatedUser(authentication);
        // Id-ul fermei vine din URL pentru ca o zona apartine mereu unei ferme.
        IrrigationZone zoneToCreate = new IrrigationZone(
                farmId,
                request.getName(),
                request.getCropType(),
                request.getAreaSize(),
                request.getMoistureThreshold()
        );
        IrrigationZone createdZone = irrigationZoneService.createIrrigationZone(farmId, zoneToCreate, currentUser);

        return new CreateIrrigationZoneResponse(
                createdZone.getId(),
                "Irrigation zone was successfully created."
        );
    }

    @GetMapping
    public List<IrrigationZone> getIrrigationZonesByFarmId(@PathVariable Long farmId, Authentication authentication) {
        // Este folosit dupa creare, ca frontend-ul sa poata afisa lista actualizata.
        return irrigationZoneService.getIrrigationZonesByFarmId(farmId, getAuthenticatedUser(authentication));
    }

    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return user;
    }
}
