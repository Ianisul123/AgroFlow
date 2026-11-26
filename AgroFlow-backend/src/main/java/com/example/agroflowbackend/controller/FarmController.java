package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.dto.CreateFarmRequest;
import com.example.agroflowbackend.dto.CreateFarmResponse;
import com.example.agroflowbackend.dto.DeleteFarmResponse;
import com.example.agroflowbackend.dto.FarmResponse;
import com.example.agroflowbackend.dto.UpdateFarmRequest;
import com.example.agroflowbackend.dto.UpdateFarmResponse;
import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.service.FarmService;
import com.example.agroflowbackend.service.RealtimeNotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/farms")
public class FarmController {

    private final FarmService farmService;
    private final RealtimeNotificationService realtimeNotificationService;

    public FarmController(FarmService farmService, RealtimeNotificationService realtimeNotificationService) {
        this.farmService = farmService;
        this.realtimeNotificationService = realtimeNotificationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateFarmResponse createFarm(@Valid @RequestBody CreateFarmRequest request, Authentication authentication) {
        AuthenticatedUser currentUser = getAuthenticatedUser(authentication);
        Farm farmToCreate = new Farm(request.getName(), request.getLocation());
        Farm createdFarm = farmService.createFarm(farmToCreate, currentUser);
        realtimeNotificationService.notifyFarmCreated(createdFarm);

        return new CreateFarmResponse(createdFarm.getId(), "Farm was successfully created.");
    }

    @GetMapping
    public List<FarmResponse> getAllFarms(Authentication authentication) {
        return farmService.getVisibleFarmResponses(getAuthenticatedUser(authentication));
    }

    @GetMapping("/{id}")
    public FarmResponse getFarmById(@PathVariable Long id, Authentication authentication) {
        return farmService.getFarmResponseById(id, getAuthenticatedUser(authentication));
    }

    @PutMapping("/{id}")
    public UpdateFarmResponse updateFarm(
            @PathVariable Long id,
            @Valid @RequestBody UpdateFarmRequest request,
            Authentication authentication
    ) {
        AuthenticatedUser currentUser = getAuthenticatedUser(authentication);
        Farm farmToUpdate = new Farm(request.getName(), request.getLocation());
        Farm updatedFarm = farmService.updateFarm(id, farmToUpdate, currentUser);
        realtimeNotificationService.notifyFarmUpdated(updatedFarm);

        return new UpdateFarmResponse(updatedFarm.getId(), "Farm was successfully updated.");
    }

    @DeleteMapping("/{id}")
    public DeleteFarmResponse deleteFarm(@PathVariable Long id, Authentication authentication) {
        Farm deletedFarm = farmService.deleteFarm(id, getAuthenticatedUser(authentication));
        realtimeNotificationService.notifyFarmDeleted(deletedFarm);
        return new DeleteFarmResponse("Farm was successfully deleted.");
    }

    private AuthenticatedUser getAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return user;
    }
}
