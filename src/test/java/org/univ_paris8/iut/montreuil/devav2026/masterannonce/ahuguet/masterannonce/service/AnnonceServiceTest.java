package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.AnnonceCreateDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.AnnonceDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.AnnonceUpdateDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.PaginatedResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ConflictException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ForbiddenException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.NotFoundException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnnonceService.
 * Uses Mockito to mock repositories — no database access.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    private AnnonceService annonceService;

    private User testUser;
    private User otherUser;
    private Category testCategory;
    private Annonce testAnnonce;

    @BeforeEach
    void setUp() {
        try {
            EntityManagerHelper.init("MasterAnnonceTestPU");
        } catch (Exception ignored) {}

        annonceService = new AnnonceService(annonceRepository, categoryRepository, userRepository);

        testUser = new User("testuser", "test@test.com", "password123", "USER");
        testUser.setId(1L);

        otherUser = new User("other", "other@test.com", "password123", "USER");
        otherUser.setId(2L);

        testCategory = new Category("Informatique");
        testCategory.setId(1L);

        testAnnonce = new Annonce("Test Title", "Test Description", "Test Address", "test@mail.com");
        testAnnonce.setId(1L);
        testAnnonce.setAuthor(testUser);
        testAnnonce.setCategory(testCategory);
        testAnnonce.setStatus(AnnonceStatus.DRAFT);
        testAnnonce.setVersion(0L);
        testAnnonce.setDate(Timestamp.from(Instant.now()));
    }

    @Test
    @DisplayName("getAnnonces - should return paginated results")
    void testGetAnnonces() {
        List<Annonce> annonces = Arrays.asList(testAnnonce);
        when(annonceRepository.findWithFilters(any(), any(), any(), eq(1), eq(10))).thenReturn(annonces);
        when(annonceRepository.countWithFilters(any(), any(), any())).thenReturn(1L);

        PaginatedResponseDTO<AnnonceDTO> result = annonceService.getAnnonces(null, null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(1, result.getTotalItems());
        assertEquals("Test Title", result.getData().get(0).getTitle());
    }

    @Test
    @DisplayName("getAnnonceById - should return DTO when found")
    void testGetAnnonceById_found() {
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        AnnonceDTO dto = annonceService.getAnnonceById(1L);

        assertNotNull(dto);
        assertEquals("Test Title", dto.getTitle());
        assertEquals("testuser", dto.getAuthorUsername());
    }

    @Test
    @DisplayName("getAnnonceById - should throw NotFoundException when not found")
    void testGetAnnonceById_notFound() {
        when(annonceRepository.findByIdFetched(999L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> annonceService.getAnnonceById(999L));
    }

    @Test
    @DisplayName("createAnnonce - should create and return DTO")
    void testCreateAnnonce() {
        when(userRepository.findById(1L)).thenReturn(testUser);
        when(categoryRepository.findById(1L)).thenReturn(testCategory);
        doAnswer(inv -> {
            Annonce a = inv.getArgument(0);
            a.setId(100L);
            return null;
        }).when(annonceRepository).save(any(Annonce.class));

        AnnonceCreateDTO dto = new AnnonceCreateDTO();
        dto.setTitle("New Annonce");
        dto.setDescription("New Description");
        dto.setCategoryId(1L);

        AnnonceDTO result = annonceService.createAnnonce(dto, 1L);

        assertNotNull(result);
        assertEquals("New Annonce", result.getTitle());
        verify(annonceRepository).save(any(Annonce.class));
    }

    @Test
    @DisplayName("createAnnonce - should throw NotFoundException for unknown user")
    void testCreateAnnonce_unknownUser() {
        when(userRepository.findById(999L)).thenReturn(null);

        AnnonceCreateDTO dto = new AnnonceCreateDTO();
        dto.setTitle("Test");
        dto.setDescription("Test");

        assertThrows(NotFoundException.class, () -> annonceService.createAnnonce(dto, 999L));
    }

    @Test
    @DisplayName("updateAnnonce - should update when author and DRAFT")
    void testUpdateAnnonce_success() {
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);
        when(categoryRepository.findById(1L)).thenReturn(testCategory);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setAdress("Updated Address");
        dto.setMail("updated@mail.com");
        dto.setCategoryId(1L);
        dto.setVersion(0L);

        AnnonceDTO result = annonceService.updateAnnonce(1L, dto, 1L);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    @DisplayName("updateAnnonce - should throw ForbiddenException when not author")
    void testUpdateAnnonce_notAuthor() {
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Updated");
        dto.setDescription("Updated");
        dto.setVersion(0L);

        assertThrows(ForbiddenException.class, () -> annonceService.updateAnnonce(1L, dto, 2L));
    }

    @Test
    @DisplayName("updateAnnonce - should throw ConflictException when PUBLISHED")
    void testUpdateAnnonce_publishedStatus() {
        testAnnonce.setStatus(AnnonceStatus.PUBLISHED);
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        AnnonceUpdateDTO dto = new AnnonceUpdateDTO();
        dto.setTitle("Updated");
        dto.setDescription("Updated");
        dto.setVersion(0L);

        assertThrows(ConflictException.class, () -> annonceService.updateAnnonce(1L, dto, 1L));
    }

    @Test
    @DisplayName("deleteAnnonce - should delete when ARCHIVED and author")
    void testDeleteAnnonce_success() {
        testAnnonce.setStatus(AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        assertDoesNotThrow(() -> annonceService.deleteAnnonce(1L, 1L));
        verify(annonceRepository).delete(1L);
    }

    @Test
    @DisplayName("deleteAnnonce - should throw ConflictException when not ARCHIVED")
    void testDeleteAnnonce_notArchived() {
        testAnnonce.setStatus(AnnonceStatus.DRAFT);
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        assertThrows(ConflictException.class, () -> annonceService.deleteAnnonce(1L, 1L));
    }

    @Test
    @DisplayName("deleteAnnonce - should throw ForbiddenException when not author")
    void testDeleteAnnonce_notAuthor() {
        testAnnonce.setStatus(AnnonceStatus.ARCHIVED);
        when(annonceRepository.findByIdFetched(1L)).thenReturn(testAnnonce);

        assertThrows(ForbiddenException.class, () -> annonceService.deleteAnnonce(1L, 2L));
    }

    @Test
    @DisplayName("deleteAnnonce - should throw NotFoundException when not found")
    void testDeleteAnnonce_notFound() {
        when(annonceRepository.findByIdFetched(999L)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> annonceService.deleteAnnonce(999L, 1L));
    }
}