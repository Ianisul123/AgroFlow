package com.example.agroflowbackend.controller;

import com.example.agroflowbackend.domain.AuthenticatedUser;
import com.example.agroflowbackend.domain.IrrigationZone;
import com.example.agroflowbackend.domain.UserRole;
import com.example.agroflowbackend.exception.FarmNotFoundException;
import com.example.agroflowbackend.exception.GlobalExceptionHandler;
import com.example.agroflowbackend.service.IrrigationZoneService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class IrrigationZoneControllerTest {

    private static final AuthenticatedUser USER = new AuthenticatedUser(10L, "Farmer One", "farmer@example.com", UserRole.USER);

    private MockMvc mockMvc;
    private IrrigationZoneService irrigationZoneService;

    @BeforeEach
    void setUp() {
        irrigationZoneService = mock(IrrigationZoneService.class);
        IrrigationZoneController irrigationZoneController = new IrrigationZoneController(irrigationZoneService);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        mockMvc = MockMvcBuilders.standaloneSetup(irrigationZoneController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setValidator(validator)
                .build();
    }

    @Test
    void createIrrigationZoneShouldReturnCreatedAndConfirmationMessage() throws Exception {
        IrrigationZone createdZone = new IrrigationZone(10L, 1L, "Greenhouse A", "Tomatoes", 12.5, 35.0);
        when(irrigationZoneService.createIrrigationZone(eq(1L), any(IrrigationZone.class), any(AuthenticatedUser.class)))
                .thenReturn(createdZone);

        String requestBody = """
                {
                  "name": "Greenhouse A",
                  "cropType": "Tomatoes",
                  "areaSize": 12.5,
                  "moistureThreshold": 35.0
                }
                """;

        mockMvc.perform(post("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.irrigationZoneId").value(10))
                .andExpect(jsonPath("$.message").value("Irrigation zone was successfully created."));
    }

    @Test
    void createIrrigationZoneWithoutNameShouldReturnValidationError() throws Exception {
        String requestBody = """
                {
                  "cropType": "Tomatoes",
                  "areaSize": 12.5,
                  "moistureThreshold": 35.0
                }
                """;

        mockMvc.perform(post("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Zone name is required."));
    }

    @Test
    void createIrrigationZoneWithInvalidAreaSizeShouldReturnValidationError() throws Exception {
        String requestBody = """
                {
                  "name": "Greenhouse A",
                  "cropType": "Tomatoes",
                  "areaSize": -1,
                  "moistureThreshold": 35.0
                }
                """;

        mockMvc.perform(post("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void createIrrigationZoneWithInvalidMoistureThresholdShouldReturnValidationError() throws Exception {
        String requestBody = """
                {
                  "name": "Greenhouse A",
                  "cropType": "Tomatoes",
                  "areaSize": 12.5,
                  "moistureThreshold": 101
                }
                """;

        mockMvc.perform(post("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed."));
    }

    @Test
    void createIrrigationZoneShouldReturnBadRequestWhenNumericValueIsNotANumber() throws Exception {
        String requestBody = """
                {
                  "name": "Greenhouse A",
                  "cropType": "Tomatoes",
                  "areaSize": "large",
                  "moistureThreshold": 35.0
                }
                """;

        mockMvc.perform(post("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Request body contains invalid values."));
    }

    @Test
    void createIrrigationZoneShouldReturnNotFoundWhenFarmDoesNotExist() throws Exception {
        when(irrigationZoneService.createIrrigationZone(eq(999L), any(IrrigationZone.class), any(AuthenticatedUser.class)))
                .thenThrow(new FarmNotFoundException("Farm not found."));

        String requestBody = """
                {
                  "name": "Greenhouse A",
                  "cropType": "Tomatoes",
                  "areaSize": 12.5,
                  "moistureThreshold": 35.0
                }
                """;

        mockMvc.perform(post("/farms/999/irrigation-zones")
                        .principal(authenticationFor(USER))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Farm not found."));
    }

    @Test
    void getIrrigationZonesByFarmIdShouldReturnZoneList() throws Exception {
        List<IrrigationZone> zones = List.of(
                new IrrigationZone(10L, 1L, "Greenhouse A", "Tomatoes", 12.5, 35.0)
        );
        when(irrigationZoneService.getIrrigationZonesByFarmId(eq(1L), any(AuthenticatedUser.class))).thenReturn(zones);

        mockMvc.perform(get("/farms/1/irrigation-zones")
                        .principal(authenticationFor(USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].farmId").value(1))
                .andExpect(jsonPath("$[0].name").value("Greenhouse A"))
                .andExpect(jsonPath("$[0].cropType").value("Tomatoes"))
                .andExpect(jsonPath("$[0].areaSize").value(12.5))
                .andExpect(jsonPath("$[0].moistureThreshold").value(35.0));
    }

    private TestingAuthenticationToken authenticationFor(AuthenticatedUser user) {
        return new TestingAuthenticationToken(user, null);
    }
}
