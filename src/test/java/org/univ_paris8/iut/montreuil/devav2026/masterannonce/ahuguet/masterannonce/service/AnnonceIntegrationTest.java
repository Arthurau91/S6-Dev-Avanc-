package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ConflictException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ForbiddenException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.NotFoundException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnnonceService.
 * Uses H2 in-memory database via MasterAnnonceTestPU.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AnnonceIntegrationTest {

    private static AnnonceService service;
    private static Long userId;
    private static Long otherUserId;
    private static Long categoryId;
    private static Long createdAnnonceId;

    @BeforeAll
    static void initAll() {
        EntityManagerHelper.init("MasterAnnonceTestPU");

        // Seed test data
        EntityManager em = EntityManagerHelper.getEntityManager();
        EntityManagerHelper.beginTransaction();

        User user = new User("integrationUser", "int@test.com", "password123", "USER");
        em.persist(user);

        User other = new User("otherUser", "other@test.com", "password123", "USER");
        em.persist(other);

        Category cat = new Category("TestCategory");
        em.persist(cat);

        EntityManagerHelper.commit();

        userId = user.getId();
        otherUserId = other.getId();
        categoryId = cat.getId();

        service = new AnnonceService(
                new AnnonceRepository(),
                new CategoryRepository(),
                new UserRepository()
        );
    }

    @AfterAll
    static void tearDown() {
        EntityManagerHelper.closeEntityManager();
        EntityManagerHelper.closeEntityManagerFactory();
    }

    @Test
    @Order(1)
    @DisplayName("Create annonce - integration")
    void testCreateAnnonce() {
        AnnonceCreateDTO dto = new AnnonceCreateDTO();
        dto.setTitle("Integration Test Annonce");
        dto.setDescription("Description for integration test");
        dto.setAdress("123 Test Street");
        dto.setMail("integration@test.com");
        dto.setCategoryId(categoryId);

        AnnonceDTO result = service.createAnnonce(dto, userId);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Integration Test Annonce", result.getTitle());
        assertEquals("DRAFT", result.getStatus());
        assertEquals("integrationUser", result.getAuthorUsername());
        assertEquals("TestCategory", result.getCategoryLabel());

        createdAnnonceId = result.getId();
    }

    @Test
    @Order(2)
    @DisplayName("Get annonce by ID - integration")
    void testGetAnnonceById() {
        AnnonceDTO result = service.getAnnonceById(createdAnnonceId);
        assertNotNull(result);
        assertEquals("Integration Test Annonce", result.getTitle());
        // Verify lazy loading works correctly
        assertNotNull(result.getCategoryLabel());
        assertNotNull(result.getAuthorUsername());
    }

    @Test
    @Order(3)
    @DisplayName("Get paginated annonces - integration")
    void testGetAnnonces() {
        PaginatedResponseDTO<AnnonceDTO> result = service.getAnnonces("Integration", null, null, 1, 10);
        assertNotNull(result);
        assertTrue(result.getTotalItems() >= 1);
        assertEquals("Integration Test Annonce", result.getData().get(0).getTitle());
    }

    @Test
    @Order(4)
    @DisplayName("Update annonce (full) - integration")
    void testUpdateAnnonce() {
        AnnonceDTO current = service.getAnnonceById(createdAnnonceId);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Updated Integration Title");
        dto.setDescription("Updated Description");
        dto.setAdress("456 Updated Street");
        dto.setMail("updated@test.com");
        dto.setCategoryId(categoryId);
        dto.setVersion(current.getVersion());

        AnnonceDTO result = service.updateAnnonce(createdAnnonceId, dto, userId);
        assertEquals("Updated Integration Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
    }

    @Test
    @Order(5)
    @DisplayName("Patch annonce status to PUBLISHED - integration")
    void testPatchAnnoncePublish() {
        AnnonceDTO current = service.getAnnonceById(createdAnnonceId);

        AnnoncePatchDTO dto = new AnnoncePatchDTO();
        dto.setStatus("PUBLISHED");
        dto.setVersion(current.getVersion());

        AnnonceDTO result = service.patchAnnonce(createdAnnonceId, dto, userId);
        assertEquals("PUBLISHED", result.getStatus());
    }

    @Test
    @Order(6)
    @DisplayName("Update should fail when PUBLISHED - integration")
    void testUpdatePublishedFails() {
        AnnonceDTO current = service.getAnnonceById(createdAnnonceId);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Should Fail");
        dto.setDescription("Should Fail");
        dto.setVersion(current.getVersion());

        assertThrows(ConflictException.class,
                () -> service.updateAnnonce(createdAnnonceId, dto, userId));
    }

    @Test
    @Order(7)
    @DisplayName("Non-author cannot modify - integration")
    void testForbiddenForNonAuthor() {
        AnnonceDTO current = service.getAnnonceById(createdAnnonceId);

        AnnoncePatchDTO dto = new AnnoncePatchDTO();
        dto.setStatus("ARCHIVED");
        dto.setVersion(current.getVersion());

        assertThrows(ForbiddenException.class,
                () -> service.patchAnnonce(createdAnnonceId, dto, otherUserId));
    }

    @Test
    @Order(8)
    @DisplayName("Patch to ARCHIVED then delete - integration")
    void testArchiveAndDelete() {
        AnnonceDTO current = service.getAnnonceById(createdAnnonceId);

        AnnoncePatchDTO patchDto = new AnnoncePatchDTO();
        patchDto.setStatus("ARCHIVED");
        patchDto.setVersion(current.getVersion());
        AnnonceDTO archived = service.patchAnnonce(createdAnnonceId, patchDto, userId);
        assertEquals("ARCHIVED", archived.getStatus());

        assertDoesNotThrow(() -> service.deleteAnnonce(createdAnnonceId, userId));
        assertThrows(NotFoundException.class, () -> service.getAnnonceById(createdAnnonceId));
    }

    @Test
    @Order(9)
    @DisplayName("Delete non-archived should fail - integration")
    void testDeleteNonArchivedFails() {
        // Create a fresh DRAFT annonce
        AnnonceCreateDTO createDto = new AnnonceCreateDTO();
        createDto.setTitle("To Delete Fail");
        createDto.setDescription("Description");
        createDto.setCategoryId(categoryId);
        AnnonceDTO created = service.createAnnonce(createDto, userId);

        assertThrows(ConflictException.class,
                () -> service.deleteAnnonce(created.getId(), userId));
    }
}