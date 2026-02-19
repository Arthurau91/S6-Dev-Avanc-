package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.BusinessException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ConflictException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ForbiddenException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.NotFoundException;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer for Annonce operations.
 * Implements business rules:
 * 1. Ownership: Only the author can modify or delete an announcement.
 * 2. State Logic: A PUBLISHED announcement cannot be modified.
 * 3. Archiving: An announcement must be ARCHIVED before physical deletion.
 * 4. Concurrency: Optimistic locking via JPA @Version.
 */
public class AnnonceService {

    private static final Logger LOG = LoggerFactory.getLogger(AnnonceService.class);
    private static final int DEFAULT_PAGE_SIZE = 10;

    private AnnonceRepository annonceRepository;
    private CategoryRepository categoryRepository;
    private UserRepository userRepository;

    public AnnonceService() {
        this.annonceRepository = new AnnonceRepository();
        this.categoryRepository = new CategoryRepository();
        this.userRepository = new UserRepository();
    }

    public AnnonceService(AnnonceRepository annonceRepository,
                          CategoryRepository categoryRepository,
                          UserRepository userRepository) {
        this.annonceRepository = annonceRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    /**
     * Get paginated list of announcements.
     */
    public PaginatedResponseDTO<AnnonceDTO> getAnnonces(String keyword, Long categoryId,
                                                         String statusStr, int page, int pageSize) {
        try {
            AnnonceStatus status = statusStr != null ? AnnonceStatus.valueOf(statusStr.toUpperCase()) : null;
            if (page < 1) page = 1;
            if (pageSize < 1) pageSize = DEFAULT_PAGE_SIZE;

            List<Annonce> annonces = annonceRepository.findWithFilters(keyword, categoryId, status, page, pageSize);
            long total = annonceRepository.countWithFilters(keyword, categoryId, status);

            List<AnnonceDTO> dtos = annonces.stream()
                    .map(AnnonceMapper::toDTO)
                    .collect(Collectors.toList());

            return new PaginatedResponseDTO<>(dtos, page, pageSize, total);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Get single announcement by ID.
     */
    public AnnonceDTO getAnnonceById(Long id) {
        try {
            Annonce annonce = annonceRepository.findByIdFetched(id);
            if (annonce == null) {
                throw new NotFoundException("Annonce avec l'id " + id + " non trouvée");
            }
            return AnnonceMapper.toDTO(annonce);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Create a new announcement.
     * The author is set from the authenticated user.
     */
    public AnnonceDTO createAnnonce(AnnonceCreateDTO dto, Long userId) {
        try {
            EntityManagerHelper.beginTransaction();

            User author = userRepository.findById(userId);
            if (author == null) {
                throw new NotFoundException("Utilisateur avec l'id " + userId + " non trouvé");
            }

            Annonce annonce = AnnonceMapper.toEntity(dto);
            annonce.setAuthor(author);
            annonce.setStatus(AnnonceStatus.DRAFT);

            if (dto.getCategoryId() != null) {
                Category cat = categoryRepository.findById(dto.getCategoryId());
                if (cat == null) {
                    throw new NotFoundException("Catégorie avec l'id " + dto.getCategoryId() + " non trouvée");
                }
                annonce.setCategory(cat);
            }

            annonceRepository.save(annonce);
            EntityManagerHelper.commit();

            LOG.info("Annonce created with id={} by user={}", annonce.getId(), userId);
            return AnnonceMapper.toDTO(annonce);
        } catch (BusinessException e) {
            EntityManagerHelper.rollback();
            throw e;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            LOG.error("Error creating annonce", e);
            throw new RuntimeException("Erreur lors de la création de l'annonce", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Full update (PUT) of an announcement.
     * Business rules:
     * - Only author can modify
     * - PUBLISHED status blocks modification
     * - Optimistic lock check via version
     */
    public AnnonceDTO updateAnnonce(Long id, AnnonceUpdateDTO dto, Long userId) {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce existing = annonceRepository.findByIdFetched(id);
            if (existing == null) {
                throw new NotFoundException("Annonce avec l'id " + id + " non trouvée");
            }

            // Business rule: Ownership
            checkOwnership(existing, userId);

            // Business rule: PUBLISHED cannot be modified
            if (existing.getStatus() == AnnonceStatus.PUBLISHED) {
                throw new ConflictException("Une annonce publiée ne peut pas être modifiée");
            }

            // Apply update
            AnnonceMapper.applyUpdate(dto, existing);

            if (dto.getCategoryId() != null) {
                Category cat = categoryRepository.findById(dto.getCategoryId());
                if (cat == null) {
                    throw new NotFoundException("Catégorie avec l'id " + dto.getCategoryId() + " non trouvée");
                }
                existing.setCategory(cat);
            }

            annonceRepository.save(existing);
            EntityManagerHelper.commit();

            LOG.info("Annonce id={} updated by user={}", id, userId);
            return AnnonceMapper.toDTO(existing);
        } catch (OptimisticLockException e) {
            EntityManagerHelper.rollback();
            throw new ConflictException("Conflit de version : l'annonce a été modifiée par un autre utilisateur");
        } catch (BusinessException e) {
            EntityManagerHelper.rollback();
            throw e;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            LOG.error("Error updating annonce id={}", id, e);
            throw new RuntimeException("Erreur lors de la mise à jour de l'annonce", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Partial update (PATCH) of an announcement.
     * Only non-null fields in the DTO are applied.
     * Business rules are the same as full update.
     * The status field can be changed via PATCH (workflow transitions).
     */
    public AnnonceDTO patchAnnonce(Long id, AnnoncePatchDTO dto, Long userId) {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce existing = annonceRepository.findByIdFetched(id);
            if (existing == null) {
                throw new NotFoundException("Annonce avec l'id " + id + " non trouvée");
            }

            // Business rule: Ownership
            checkOwnership(existing, userId);

            // Business rule: PUBLISHED cannot be modified (except status change to ARCHIVED)
            if (existing.getStatus() == AnnonceStatus.PUBLISHED) {
                boolean onlyStatusChange = dto.getTitle() == null && dto.getDescription() == null
                        && dto.getAdress() == null && dto.getMail() == null && dto.getCategoryId() == null;
                if (!onlyStatusChange) {
                    throw new ConflictException("Une annonce publiée ne peut pas être modifiée (sauf changement de statut vers ARCHIVED)");
                }
            }

            // Apply partial update
            AnnonceMapper.applyPatch(dto, existing);

            // Handle status change
            if (dto.getStatus() != null) {
                AnnonceStatus newStatus = AnnonceStatus.valueOf(dto.getStatus().toUpperCase());
                existing.setStatus(newStatus);
            }

            if (dto.getCategoryId() != null) {
                Category cat = categoryRepository.findById(dto.getCategoryId());
                if (cat == null) {
                    throw new NotFoundException("Catégorie avec l'id " + dto.getCategoryId() + " non trouvée");
                }
                existing.setCategory(cat);
            }

            annonceRepository.save(existing);
            EntityManagerHelper.commit();

            LOG.info("Annonce id={} patched by user={}", id, userId);
            return AnnonceMapper.toDTO(existing);
        } catch (OptimisticLockException e) {
            EntityManagerHelper.rollback();
            throw new ConflictException("Conflit de version : l'annonce a été modifiée par un autre utilisateur");
        } catch (BusinessException e) {
            EntityManagerHelper.rollback();
            throw e;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            LOG.error("Error patching annonce id={}", id, e);
            throw new RuntimeException("Erreur lors de la mise à jour partielle de l'annonce", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Delete an announcement.
     * Business rules:
     * - Only author can delete
     * - Must be ARCHIVED before physical deletion
     */
    public void deleteAnnonce(Long id, Long userId) {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce existing = annonceRepository.findByIdFetched(id);
            if (existing == null) {
                throw new NotFoundException("Annonce avec l'id " + id + " non trouvée");
            }

            // Business rule: Ownership
            checkOwnership(existing, userId);

            // Business rule: Must be ARCHIVED before deletion
            if (existing.getStatus() != AnnonceStatus.ARCHIVED) {
                throw new ConflictException("L'annonce doit être archivée avant d'être supprimée (statut actuel: "
                        + existing.getStatus() + ")");
            }

            annonceRepository.delete(id);
            EntityManagerHelper.commit();

            LOG.info("Annonce id={} deleted by user={}", id, userId);
        } catch (BusinessException e) {
            EntityManagerHelper.rollback();
            throw e;
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            LOG.error("Error deleting annonce id={}", id, e);
            throw new RuntimeException("Erreur lors de la suppression de l'annonce", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    // --- Private helpers ---

    private void checkOwnership(Annonce annonce, Long userId) {
        if (annonce.getAuthor() == null || !annonce.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("Seul l'auteur de l'annonce peut effectuer cette action");
        }
    }

    // --- Setters for test injection ---

    public void setAnnonceRepository(AnnonceRepository repo) { this.annonceRepository = repo; }
    public void setCategoryRepository(CategoryRepository repo) { this.categoryRepository = repo; }
    public void setUserRepository(UserRepository repo) { this.userRepository = repo; }
}