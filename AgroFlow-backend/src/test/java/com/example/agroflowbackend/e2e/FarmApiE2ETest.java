package com.example.agroflowbackend.e2e;

import com.example.agroflowbackend.repository.FarmJpaRepository;
import com.example.agroflowbackend.repository.IrrigationZoneJpaRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FarmApiE2ETest {

    private static final String ADMIN_EMAIL = "admin-e2e@agroflow.local";
    private static final String ADMIN_PASSWORD = "Admin123";

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Autowired
    private FarmJpaRepository farmJpaRepository;

    @Autowired
    private IrrigationZoneJpaRepository irrigationZoneJpaRepository;

    @BeforeEach
    void cleanData() {
        irrigationZoneJpaRepository.deleteAll();
        farmJpaRepository.deleteAll();
    }

    @Test
    void unauthenticatedUserCannotAccessFarms() throws Exception {
        mockMvc.perform(get("/farms"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Authentication is required."));
    }

    @Test
    void userCanCreateReadAndUpdateFarmThroughApi() throws Exception {
        String token = registerUser("Farm Owner", uniqueEmail("owner"));

        Long farmId = createFarm(token, "North Field", "Eindhoven");

        mockMvc.perform(get("/farms")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(farmId))
                .andExpect(jsonPath("$[0].name").value("North Field"))
                .andExpect(jsonPath("$[0].location").value("Eindhoven"));

        mockMvc.perform(get("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(farmId))
                .andExpect(jsonPath("$.name").value("North Field"));

        mockMvc.perform(put("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Updated North Field",
                                  "location": "Tilburg"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.farmId").value(farmId))
                .andExpect(jsonPath("$.message").value("Farm was successfully updated."));

        mockMvc.perform(get("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated North Field"))
                .andExpect(jsonPath("$.location").value("Tilburg"));
    }

    @Test
    void validationErrorsAreReturnedWhenCreatingInvalidFarm() throws Exception {
        String token = registerUser("Validation User", uniqueEmail("validation"));

        mockMvc.perform(post("/farms")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "location": "Eindhoven"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed."))
                .andExpect(jsonPath("$.errors[0].field").value("name"))
                .andExpect(jsonPath("$.errors[0].message").value("Farm name is required."));
    }

    @Test
    void userCannotSeeOtherUsersFarm() throws Exception {
        String ownerToken = registerUser("Private Owner", uniqueEmail("private-owner"));
        String otherUserToken = registerUser("Other User", uniqueEmail("other-user"));
        Long farmId = createFarm(ownerToken, "Private Farm", "Breda");

        mockMvc.perform(get("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUserToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Farm not found."));

        MvcResult result = mockMvc.perform(get("/farms")
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUserToken)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode farms = objectMapper.readTree(result.getResponse().getContentAsString());
        assertThat(farms).isEmpty();
    }

    @Test
    void userCanCreateAndReadIrrigationZoneThroughApi() throws Exception {
        String token = registerUser("Zone Owner", uniqueEmail("zone-owner"));
        Long farmId = createFarm(token, "Greenhouse Farm", "Helmond");

        MvcResult createZoneResult = mockMvc.perform(post("/farms/{farmId}/irrigation-zones", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Greenhouse A",
                                  "cropType": "Tomatoes",
                                  "areaSize": 12.5,
                                  "moistureThreshold": 35.0
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Irrigation zone was successfully created."))
                .andReturn();

        JsonNode createZoneResponse = objectMapper.readTree(createZoneResult.getResponse().getContentAsString());
        Long zoneId = createZoneResponse.get("irrigationZoneId").asLong();

        mockMvc.perform(get("/farms/{farmId}/irrigation-zones", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(zoneId))
                .andExpect(jsonPath("$[0].farmId").value(farmId))
                .andExpect(jsonPath("$[0].name").value("Greenhouse A"))
                .andExpect(jsonPath("$[0].cropType").value("Tomatoes"))
                .andExpect(jsonPath("$[0].areaSize").value(12.5))
                .andExpect(jsonPath("$[0].moistureThreshold").value(35.0));
    }

    @Test
    void userCannotManageIrrigationZonesForAnotherUsersFarm() throws Exception {
        String ownerToken = registerUser("Zone Private Owner", uniqueEmail("zone-private-owner"));
        String otherUserToken = registerUser("Zone Other User", uniqueEmail("zone-other-user"));
        Long farmId = createFarm(ownerToken, "Private Zone Farm", "Venlo");

        mockMvc.perform(post("/farms/{farmId}/irrigation-zones", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUserToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Restricted Zone",
                                  "cropType": "Lettuce",
                                  "areaSize": 8.0,
                                  "moistureThreshold": 45.0
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Farm not found."));

        mockMvc.perform(get("/farms/{farmId}/irrigation-zones", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(otherUserToken)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Farm not found."));
    }

    @Test
    void normalUserCannotDeleteFarmButAdminCan() throws Exception {
        String userToken = registerUser("Delete Owner", uniqueEmail("delete-owner"));
        Long farmId = createFarm(userToken, "Delete Candidate", "Den Bosch");

        mockMvc.perform(delete("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(userToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied."));

        String adminToken = login(ADMIN_EMAIL, ADMIN_PASSWORD);

        mockMvc.perform(delete("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Farm was successfully deleted."));

        mockMvc.perform(get("/farms/{id}", farmId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(adminToken)))
                .andExpect(status().isNotFound());
    }

    private Long createFarm(String token, String name, String location) throws Exception {
        MvcResult result = mockMvc.perform(post("/farms")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "location": "%s"
                                }
                                """.formatted(name, location)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Farm was successfully created."))
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("farmId").asLong();
    }

    private String registerUser(String name, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "email": "%s",
                                  "password": "Password1"
                                }
                                """.formatted(name, email)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenFrom(result);
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "%s"
                                }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn();

        return tokenFrom(result);
    }

    private String tokenFrom(MvcResult result) throws Exception {
        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString());
        return response.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String uniqueEmail(String prefix) {
        return prefix + "-" + System.nanoTime() + "@example.com";
    }
}
