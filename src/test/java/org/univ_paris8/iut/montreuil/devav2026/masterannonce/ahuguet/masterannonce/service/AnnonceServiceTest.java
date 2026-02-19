package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ConflictException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ForbiddenException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ResourceNotFoundException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AnnonceService.
 * Tests business rules strictly with Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepository;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AnnonceMapper annonceMapper;

    @InjectMocks
    private AnnonceService annonceService;

    private User author;
    private User otherUser;
    private Category category;
    private Annonce annonce;
    private AnnonceDTO annonceDTO;

    @BeforeEach
    void setUp() {
        author = new User("testuser", "test@example.com", "encodedpwd", "USER");
        author.setId(1L);

        otherUser = new User("other", "other@example.com", "encodedpwd", "USER");
        otherUser.setId(2L);

        category = new Category("Informatique");
        category.setId(1L);

        annonce = new Annonce("Titre", "Description", "Adresse", "mail@test.com");
        annonce.setId(1L);
        annonce.setAuthor(author);
        annonce.setCategory(category);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setVersion(0L);
        annonce.setDate(LocalDateTime.now());

        annonceDTO = new AnnonceDTO();
        annonceDTO.setId(1L);
        annonceDTO.setTitle("Titre");
        annonceDTO.setStatus("DRAFT");
    }

    // --- GET tests ---

    @Test
    @DisplayName("getAnnonceById - returns DTO when found")
    void getAnnonceById_found() {
        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
        when(annonceMapper.toDTO(annonce)).thenReturn(annonceDTO);

        AnnonceDTO result = annonceService.getAnnonceById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(annonceRepository).findById(1L);
    }

    @Test
    @DisplayName("getAnnonceById - throws ResourceNotFoundException when not found")
    void getAnnonceById_notFound() {
        when(annonceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> annonceService.getAnnonceById(99L));
    }

    @Test
    @SuppressWarnings("unchecked")
    @DisplayName("getAnnonces - returns paginated results")
    void getAnnonces_paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Annonce> page = new PageImpl<>(List.of(annonce), pageable, 1);

        when(annonceRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);
        when(annonceMapper.toDTO(annonce)).thenReturn(annonceDTO);

        Page<AnnonceDTO> result = annonceService.getAnnonces(null, null, null, null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    // --- CREATE tests ---

    @Test
    @DisplayName("createAnnonce - successful creation")
    void createAnnonce_success() {
        AnnonceCreateDTO createDTO = new AnnonceCreateDTO();
        createDTO.setTitle("New");
        createDTO.setDescription("Desc");
        createDTO.setCategoryId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(annonceMapper.toEntity(createDTO)).thenReturn(new Annonce());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
        when(annonceMapper.toDTO(any(Annonce.class))).thenReturn(annonceDTO);

        AnnonceDTO result = annonceService.createAnnonce(createDTO, 1L);

        assertNotNull(result);
        verify(annonceRepository).save(any(Annonce.class));
    }

    @Test
    @DisplayName("createAnnonce - throws when user not found")
    void createAnnonce_userNotFound() {
        AnnonceCreateDTO createDTO = new AnnonceCreateDTO();
        createDTO.setTitle("New");
        createDTO.setDescription("Desc");

        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> annonceService.createAnnonce(createDTO, 99L));
    }

    // --- UPDATE tests ---

    @Test
    @DisplayName("updateAnnonce - successful update by author")
    void updateAnnonce_success() {
        AnnonceUpdateDTO updateDTO = new AnnonceUpdateDTO();
        updateDTO.setTitle("Updated");
        updateDTO.setDescription("Updated desc");
        updateDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
        when(annonceMapper.toDTO(any(Annonce.class))).thenReturn(annonceDTO);

        AnnonceDTO result = annonceService.updateAnnonce(1L, updateDTO, 1L);

        assertNotNull(result);
        verify(annonceMapper).updateEntityFromDTO(eq(updateDTO), eq(annonce));
    }

    @Test
    @DisplayName("updateAnnonce - throws ForbiddenException when not author")
    void updateAnnonce_notAuthor() {
        AnnonceUpdateDTO updateDTO = new AnnonceUpdateDTO();
        updateDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ForbiddenException.class, () -> annonceService.updateAnnonce(1L, updateDTO, 2L));
    }

    @Test
    @DisplayName("updateAnnonce - throws ConflictException when PUBLISHED")
    void updateAnnonce_published() {
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        AnnonceUpdateDTO updateDTO = new AnnonceUpdateDTO();
        updateDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ConflictException.class, () -> annonceService.updateAnnonce(1L, updateDTO, 1L));
    }

    // --- PATCH tests ---

    @Test
    @DisplayName("patchAnnonce - successful partial update")
    void patchAnnonce_success() {
        AnnoncePatchDTO patchDTO = new AnnoncePatchDTO();
        patchDTO.setTitle("Patched title");
        patchDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
        when(annonceMapper.toDTO(any(Annonce.class))).thenReturn(annonceDTO);

        AnnonceDTO result = annonceService.patchAnnonce(1L, patchDTO, 1L, false);

        assertNotNull(result);
        verify(annonceMapper).patchEntityFromDTO(eq(patchDTO), eq(annonce));
    }

    @Test
    @DisplayName("patchAnnonce - PUBLISHED annonce blocks content modification")
    void patchAnnonce_publishedBlocksContentChange() {
        annonce.setStatus(AnnonceStatus.PUBLISHED);
        AnnoncePatchDTO patchDTO = new AnnoncePatchDTO();
        patchDTO.setTitle("Change title");
        patchDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ConflictException.class, () -> annonceService.patchAnnonce(1L, patchDTO, 1L, false));
    }

    @Test
    @DisplayName("patchAnnonce - only ADMIN can archive")
    void patchAnnonce_onlyAdminCanArchive() {
        AnnoncePatchDTO patchDTO = new AnnoncePatchDTO();
        patchDTO.setStatus("ARCHIVED");
        patchDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ForbiddenException.class, () -> annonceService.patchAnnonce(1L, patchDTO, 1L, false));
    }

    @Test
    @DisplayName("patchAnnonce - ADMIN can archive")
    void patchAnnonce_adminCanArchive() {
        AnnoncePatchDTO patchDTO = new AnnoncePatchDTO();
        patchDTO.setStatus("ARCHIVED");
        patchDTO.setVersion(0L);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));
        when(annonceRepository.save(any(Annonce.class))).thenReturn(annonce);
        when(annonceMapper.toDTO(any(Annonce.class))).thenReturn(annonceDTO);

        AnnonceDTO result = annonceService.patchAnnonce(1L, patchDTO, 1L, true);

        assertNotNull(result);
    }

    // --- DELETE tests ---

    @Test
    @DisplayName("deleteAnnonce - successful deletion of ARCHIVED annonce")
    void deleteAnnonce_success() {
        annonce.setStatus(AnnonceStatus.ARCHIVED);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        annonceService.deleteAnnonce(1L, 1L);

        verify(annonceRepository).delete(annonce);
    }

    @Test
    @DisplayName("deleteAnnonce - throws ConflictException when not ARCHIVED")
    void deleteAnnonce_notArchived() {
        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ConflictException.class, () -> annonceService.deleteAnnonce(1L, 1L));
    }

    @Test
    @DisplayName("deleteAnnonce - throws ForbiddenException when not author")
    void deleteAnnonce_notAuthor() {
        annonce.setStatus(AnnonceStatus.ARCHIVED);

        when(annonceRepository.findById(1L)).thenReturn(Optional.of(annonce));

        assertThrows(ForbiddenException.class, () -> annonceService.deleteAnnonce(1L, 2L));
    }
}
