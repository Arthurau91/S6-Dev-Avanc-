package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.specification.AnnonceSpecifications;

import java.time.LocalDateTime;

/**
 * Service layer for Annonce operations.
 * Business rules:
 * 1. Only the author can modify/delete their own Annonce.
 * 2. Only ADMIN can archive.
 * 3. PUBLISHED annonces cannot be modified (content).
 * 4. Optimistic locking via JPA @Version.
 * 5. Must be ARCHIVED before deletion.
 */
@Service
@Transactional(readOnly = true)
public class AnnonceService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceService.class);

    private final AnnonceRepository annonceRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final AnnonceMapper annonceMapper;

    public AnnonceService(AnnonceRepository annonceRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository,
                          AnnonceMapper annonceMapper) {
        this.annonceRepository = annonceRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
        this.annonceMapper = annonceMapper;
    }

    /**
     * Get paginated and filtered list of annonces using Specifications.
     */
    public Page<AnnonceDTO> getAnnonces(String keyword, String statusStr, Long categoryId,
                                         Long authorId, LocalDateTime fromDate, LocalDateTime toDate,
                                         Pageable pageable) {
        Specification<Annonce> spec = Specification.where(null);

        if (keyword != null && !keyword.isBlank()) {
            spec = spec.and(AnnonceSpecifications.hasKeyword(keyword));
        }
        if (statusStr != null && !statusStr.isBlank()) {
            AnnonceStatus status = AnnonceStatus.valueOf(statusStr.toUpperCase());
            spec = spec.and(AnnonceSpecifications.hasStatus(status));
        }
        if (categoryId != null) {
            spec = spec.and(AnnonceSpecifications.hasCategoryId(categoryId));
        }
        if (authorId != null) {
            spec = spec.and(AnnonceSpecifications.hasAuthorId(authorId));
        }
        if (fromDate != null) {
            spec = spec.and(AnnonceSpecifications.createdAfter(fromDate));
        }
        if (toDate != null) {
            spec = spec.and(AnnonceSpecifications.createdBefore(toDate));
        }

        Page<Annonce> page = annonceRepository.findAll(spec, pageable);
        return page.map(annonceMapper::toDTO);
    }

    /**
     * Get single annonce by ID.
     */
    public AnnonceDTO getAnnonceById(Long id) {
        Annonce annonce = findAnnonceOrThrow(id);
        return annonceMapper.toDTO(annonce);
    }

    /**
     * Create a new annonce. Author is set from the authenticated user.
     */
    @Transactional
    public AnnonceDTO createAnnonce(AnnonceCreateDTO dto, Long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur avec l'id " + userId + " non trouvé"));

        Annonce annonce = annonceMapper.toEntity(dto);
        annonce.setAuthor(author);
        annonce.setStatus(AnnonceStatus.DRAFT);
        annonce.setDate(LocalDateTime.now());

        if (dto.getCategoryId() != null) {
            Category cat = findCategoryOrThrow(dto.getCategoryId());
            annonce.setCategory(cat);
        }

        Annonce saved = annonceRepository.save(annonce);
        LOG.info("Annonce created with id={} by user={}", saved.getId(), userId);
        return annonceMapper.toDTO(saved);
    }

    /**
     * Full update (PUT). Business rules: ownership + not PUBLISHED.
     */
    @Transactional
    public AnnonceDTO updateAnnonce(Long id, AnnonceUpdateDTO dto, Long userId) {
        Annonce existing = findAnnonceOrThrow(id);

        checkOwnership(existing, userId);
        checkNotPublished(existing);

        annonceMapper.updateEntityFromDTO(dto, existing);

        if (dto.getCategoryId() != null) {
            Category cat = findCategoryOrThrow(dto.getCategoryId());
            existing.setCategory(cat);
        }

        Annonce saved = annonceRepository.save(existing);
        LOG.info("Annonce id={} updated by user={}", id, userId);
        return annonceMapper.toDTO(saved);
    }

    /**
     * Partial update (PATCH). Only non-null fields applied.
     * Status changes are allowed (e.g., DRAFT -> PUBLISHED).
     */
    @Transactional
    public AnnonceDTO patchAnnonce(Long id, AnnoncePatchDTO dto, Long userId, boolean isAdmin) {
        Annonce existing = findAnnonceOrThrow(id);

        checkOwnership(existing, userId);

        // If PUBLISHED, only allow status change (to ARCHIVED, by admin)
        if (existing.getStatus() == AnnonceStatus.PUBLISHED) {
            boolean onlyStatusChange = dto.getTitle() == null && dto.getDescription() == null
                    && dto.getAdress() == null && dto.getMail() == null && dto.getCategoryId() == null;
            if (!onlyStatusChange) {
                throw new ConflictException("Une annonce publiée ne peut pas être modifiée (sauf changement de statut vers ARCHIVED)");
            }
        }

        // Archive: only ADMIN can archive
        if (dto.getStatus() != null && "ARCHIVED".equalsIgnoreCase(dto.getStatus())) {
            if (!isAdmin) {
                throw new ForbiddenException("Seul un administrateur peut archiver une annonce");
            }
        }

        annonceMapper.patchEntityFromDTO(dto, existing);

        if (dto.getStatus() != null) {
            AnnonceStatus newStatus = AnnonceStatus.valueOf(dto.getStatus().toUpperCase());
            existing.setStatus(newStatus);
        }

        if (dto.getCategoryId() != null) {
            Category cat = findCategoryOrThrow(dto.getCategoryId());
            existing.setCategory(cat);
        }

        Annonce saved = annonceRepository.save(existing);
        LOG.info("Annonce id={} patched by user={}", id, userId);
        return annonceMapper.toDTO(saved);
    }

    /**
     * Delete an annonce. Must be ARCHIVED first.
     */
    @Transactional
    public void deleteAnnonce(Long id, Long userId) {
        Annonce existing = findAnnonceOrThrow(id);

        checkOwnership(existing, userId);

        if (existing.getStatus() != AnnonceStatus.ARCHIVED) {
            throw new ConflictException("L'annonce doit être archivée avant d'être supprimée (statut actuel: "
                    + existing.getStatus() + ")");
        }

        annonceRepository.delete(existing);
        LOG.info("Annonce id={} deleted by user={}", id, userId);
    }

    // --- Private helpers ---

    private Annonce findAnnonceOrThrow(Long id) {
        return annonceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Annonce avec l'id " + id + " non trouvée"));
    }

    private Category findCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie avec l'id " + categoryId + " non trouvée"));
    }

    private void checkOwnership(Annonce annonce, Long userId) {
        if (annonce.getAuthor() == null || !annonce.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur de l'annonce peut effectuer cette action");
        }
    }

    private void checkNotPublished(Annonce annonce) {
        if (annonce.getStatus() == AnnonceStatus.PUBLISHED) {
            throw new ConflictException("Une annonce publiée ne peut pas être modifiée");
        }
    }
}
