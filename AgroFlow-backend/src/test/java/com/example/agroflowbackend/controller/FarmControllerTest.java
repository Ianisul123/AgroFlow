package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.Farm;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.dto.FarmResponse;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.exception.GlobalExceptionHandler;
import com.example.agroflowbackend.service.FarmService;
import com.example.agroflowbackend.service.RealtimeNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FarmControllerTest {

    private static final AuthenticatedUser USER = new AuthenticatedUser(10L, "Farmer One", "farmer@example.com", UserRole.USER);
    private static final AuthenticatedUser ADMIN = new AuthenticatedUser(99L, "Admin", "admin@example.com", UserRole.ADMIN);

    private MockMvc mockMvc;
    private FarmService farmService;
    private RealtimeNotificationService realtimeNotificationService;

    @BeforeEach
    void setUp() {
        farmService = mock(FarmService.class);
        realtimeNotificationService = mock(RealtimeNotificationService.class);
        FarmController farmController = new FarmController(farmService, realtimeNotificationService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(farmController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createFarmShouldReturnCreatedAndConfirmationMessage() throws Exception {
        Farm createdFarm = new Farm(1L, "North Field", "Eindhoven", USER.getId());
        when(farmService.createFarm(any(Farm.class), any(AuthenticatedUser.class))).thenReturn(createdFarm);

        String requestBody = """
                {
                  "name": "North Field",
                  "location": "Eindhoven"
                }
                """;

        mockMvc.perform(post("/farms")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.farmId").value(1))
                .andExpect(jsonPath("$.message").value("Farm was successfully created."));

        verify(realtimeNotificationService).notifyFarmCreated(createdFarm);
    }

    @Test
    void createFarmWithoutNameShouldReturnValidationError() throws Exception {
        String requestBody = """
                {
                  "location": "Eindhoven"
                }
                """;

        mockMvc.perform(post("/farms")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Farm name is required."));

        verify(realtimeNotificationService, never()).notifyFarmCreated(any(Farm.class));
    }

    @Test
    void getAllFarmsShouldReturnFarmOverviewList() throws Exception {
        List<FarmResponse> farms = List.of(
                new FarmResponse(1L, "South Field", "Tilburg", USER.getId(), "Farmer One", "farmer@example.com")
        );

        when(farmService.getVisibleFarmResponses(any(AuthenticatedUser.class))).thenReturn(farms);

        mockMvc.perform(get("/farms")
                        .principal(authenticationFor(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("South Field"))
                .andExpect(jsonPath("$[0].location").value("Tilburg"))
                .andExpect(jsonPath("$[0].ownerName").value("Farmer One"));
    }

    @Test
    void getFarmByIdShouldReturnFarmDetails() throws Exception {
        FarmResponse farm = new FarmResponse(1L, "North Field", "Eindhoven", USER.getId(), "Farmer One", "farmer@example.com");
        when(farmService.getFarmResponseById(eq(1L), any(AuthenticatedUser.class))).thenReturn(farm);

        mockMvc.perform(get("/farms/1")
                        .principal(authenticationFor(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("North Field"))
                .andExpect(jsonPath("$.location").value("Eindhoven"))
                .andExpect(jsonPath("$.ownerName").value("Farmer One"));
    }

    @Test
    void getFarmByIdShouldReturnNotFoundWhenFarmDoesNotExist() throws Exception {
        when(farmService.getFarmResponseById(eq(999L), any(AuthenticatedUser.class)))
                .thenThrow(new FarmNotFoundException("Farm not found."));

        mockMvc.perform(get("/farms/999")
                        .principal(authenticationFor(USER)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Farm not found."));
    }

    @Test
    void updateFarmShouldReturnSuccessMessage() throws Exception {
        Farm updatedFarm = new Farm(1L, "Updated Farm", "Amsterdam", USER.getId());
        when(farmService.updateFarm(any(Long.class), any(Farm.class), any(AuthenticatedUser.class))).thenReturn(updatedFarm);

        String requestBody = """
                {
                  "name": "Updated Farm",
                  "location": "Amsterdam"
                }
                """;

        mockMvc.perform(put("/farms/1")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(1))
                .andExpect(jsonPath("$.message").value("Farm was successfully updated."));

        verify(realtimeNotificationService).notifyFarmUpdated(updatedFarm);
    }

    @Test
    void updateFarmShouldReturnValidationErrorWhenNameIsMissing() throws Exception {
        String requestBody = """
                {
                  "location": "Amsterdam"
                }
                """;

        mockMvc.perform(put("/farms/1")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."));

        verify(realtimeNotificationService, never()).notifyFarmUpdated(any(Farm.class));
    }

    @Test
    void deleteFarmShouldReturnSuccessMessage() throws Exception {
        Farm deletedFarm = new Farm(1L, "Deleted Farm", "Breda", USER.getId());
        when(farmService.deleteFarm(eq(1L), any(AuthenticatedUser.class))).thenReturn(deletedFarm);

        mockMvc.perform(delete("/farms/1")
                        .principal(authenticationFor(ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Farm was successfully deleted."));

        verify(realtimeNotificationService).notifyFarmDeleted(deletedFarm);
    }

    @Test
    void deleteFarmShouldReturnNotFoundWhenFarmDoesNotExist() throws Exception {
        when(farmService.deleteFarm(eq(999L), any(AuthenticatedUser.class)))
                .thenThrow(new FarmNotFoundException("Farm not found."));

        mockMvc.perform(delete("/farms/999")
                        .principal(authenticationFor(ADMIN)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Farm not found."));

        verify(realtimeNotificationService, never()).notifyFarmDeleted(any(Farm.class));
    }

    private TestingAuthenticationToken authenticationFor(AuthenticatedUser user) {
        return new TestingAuthenticationToken(user, null);
    }
}
