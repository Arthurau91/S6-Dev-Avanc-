package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginRequestDTO;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests using SpringBootTest + MockMvc + Testcontainers (PostgreSQL).
 * Scenarios: Login, Protected endpoint w/o token (401), Invalid token (401),
 * Insufficient Role (403), Full CRUD flow.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("masterannonce_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String userToken;
    private static String adminToken;
    private static Long createdAnnonceId;

    // --- Authentication Tests ---

    @Test
    @Order(1)
    @DisplayName("Login as admin - returns JWT token")
    void loginAsAdmin() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin", "admin123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(3600))
                .andReturn();

        String body = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @Order(2)
    @DisplayName("Login as user - returns JWT token")
    void loginAsUser() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("testuser", "password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andReturn();

        String body = result.getResponse().getContentAsString();
        userToken = objectMapper.readTree(body).get("token").asText();
    }

    @Test
    @Order(3)
    @DisplayName("Login with invalid credentials - 401")
    void loginInvalidCredentials() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin", "wrongpassword");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // --- Protected Endpoint Tests ---

    @Test
    @Order(4)
    @DisplayName("Access protected endpoint without token - 401")
    void accessProtectedWithoutToken() throws Exception {
        mockMvc.perform(post("/api/annonces")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"description\":\"Desc\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(5)
    @DisplayName("Access protected endpoint with invalid token - 401")
    void accessProtectedWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/annonces")
                        .header("Authorization", "Bearer invalid.jwt.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Test\",\"description\":\"Desc\"}"))
                .andExpect(status().isUnauthorized());
    }

    // --- Full CRUD Flow ---

    @Test
    @Order(10)
    @DisplayName("CREATE annonce as user")
    void createAnnonce() throws Exception {
        String body = """
                {
                    "title": "Vends PC portable",
                    "description": "PC en excellent état",
                    "adress": "123 Rue de Montreuil",
                    "mail": "user@example.com",
                    "categoryId": 1
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/annonces")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Vends PC portable"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        createdAnnonceId = objectMapper.readTree(responseBody).get("id").asLong();
    }

    @Test
    @Order(11)
    @DisplayName("GET annonce by ID")
    void getAnnonceById() throws Exception {
        mockMvc.perform(get("/api/annonces/" + createdAnnonceId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdAnnonceId))
                .andExpect(jsonPath("$.title").value("Vends PC portable"));
    }

    @Test
    @Order(12)
    @DisplayName("GET paginated list of annonces")
    void listAnnonces() throws Exception {
        mockMvc.perform(get("/api/annonces")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").isNumber());
    }

    @Test
    @Order(13)
    @DisplayName("UPDATE annonce (PUT) as author")
    void updateAnnonce() throws Exception {
        String body = """
                {
                    "title": "Vends PC portable - MAJ",
                    "description": "PC en très bon état",
                    "adress": "123 Rue de Montreuil",
                    "mail": "user@example.com",
                    "categoryId": 1,
                    "version": 0
                }
                """;

        mockMvc.perform(put("/api/annonces/" + createdAnnonceId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Vends PC portable - MAJ"));
    }

    @Test
    @Order(14)
    @DisplayName("PATCH annonce - partial update")
    void patchAnnonce() throws Exception {
        String body = """
                {
                    "title": "Titre patché",
                    "version": 1
                }
                """;

        mockMvc.perform(patch("/api/annonces/" + createdAnnonceId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Titre patché"));
    }

    @Test
    @Order(15)
    @DisplayName("PATCH archive by non-admin - 403")
    void patchArchiveByNonAdmin() throws Exception {
        String body = """
                {
                    "status": "ARCHIVED",
                    "version": 2
                }
                """;

        mockMvc.perform(patch("/api/annonces/" + createdAnnonceId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    @Order(16)
    @DisplayName("DELETE non-archived annonce - 409 Conflict")
    void deleteNonArchivedAnnonce() throws Exception {
        mockMvc.perform(delete("/api/annonces/" + createdAnnonceId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isConflict());
    }

    @Test
    @Order(20)
    @DisplayName("GET /api/meta/annonces - introspection")
    void getMetadata() throws Exception {
        mockMvc.perform(get("/api/meta/annonces"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.entity").value("Annonce"))
                .andExpect(jsonPath("$.fields").isArray())
                .andExpect(jsonPath("$.sortableFields").isArray());
    }

    @Test
    @Order(21)
    @DisplayName("GET /actuator/health")
    void actuatorHealth() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
