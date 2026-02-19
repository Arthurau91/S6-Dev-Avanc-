package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.UserPrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * REST Controller for Annonce CRUD operations.
 * Base path: /api/annonces
 */
@RestController
@RequestMapping("/api/annonces")
@Tag(name = "Annonces", description = "CRUD operations for announcements")
public class AnnonceController {

    private static final Set<String> SORTABLE_FIELDS;

    static {
        // Introspection: validate sort fields via Reflection
        SORTABLE_FIELDS = Arrays.stream(Annonce.class.getDeclaredFields())
                .map(Field::getName)
                .collect(Collectors.toSet());
    }

    private final AnnonceService annonceService;

    public AnnonceController(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    /**
     * GET /api/annonces — Paginated list with dynamic search (Specifications).
     */
    @GetMapping
    @Operation(summary = "List announcements (paginated with dynamic filters)", responses = {
            @ApiResponse(responseCode = "200", description = "Paginated list of announcements")
    })
    public ResponseEntity<Page<AnnonceDTO>> listAnnonces(
            @Parameter(description = "Search keyword on title/description") @RequestParam(required = false) String q,
            @Parameter(description = "Filter by status") @RequestParam(required = false) String status,
            @Parameter(description = "Filter by category ID") @RequestParam(required = false) Long categoryId,
            @Parameter(description = "Filter by author ID") @RequestParam(required = false) Long authorId,
            @Parameter(description = "From date (ISO format)") @RequestParam(required = false) LocalDateTime fromDate,
            @Parameter(description = "To date (ISO format)") @RequestParam(required = false) LocalDateTime toDate,
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Sort field") @RequestParam(defaultValue = "date") String sortBy,
            @Parameter(description = "Sort direction (asc/desc)") @RequestParam(defaultValue = "desc") String sortDir) {

        // Validate sort field via Reflection-based introspection
        if (!SORTABLE_FIELDS.contains(sortBy)) {
            throw new IllegalArgumentException("Champ de tri invalide: '" + sortBy + "'. Champs autorisés: " + SORTABLE_FIELDS);
        }

        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AnnonceDTO> result = annonceService.getAnnonces(q, status, categoryId, authorId, fromDate, toDate, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/annonces/{id} — Detail view
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get announcement by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement found"),
            @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    public ResponseEntity<AnnonceDTO> getAnnonce(@PathVariable Long id) {
        return ResponseEntity.ok(annonceService.getAnnonceById(id));
    }

    /**
     * POST /api/annonces — Create announcement (requires authentication)
     */
    @PostMapping
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create a new announcement", responses = {
            @ApiResponse(responseCode = "201", description = "Announcement created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AnnonceDTO> createAnnonce(@Valid @RequestBody AnnonceCreateDTO dto,
                                                     Authentication authentication) {
        Long userId = extractUserId(authentication);
        AnnonceDTO created = annonceService.createAnnonce(dto, userId);
        return ResponseEntity.created(URI.create("/api/annonces/" + created.getId()))
                .body(created);
    }

    /**
     * PUT /api/annonces/{id} — Full update (requires authentication)
     */
    @PutMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update an announcement (full)", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Business conflict or optimistic lock")
    })
    public ResponseEntity<AnnonceDTO> updateAnnonce(@PathVariable Long id,
                                                     @Valid @RequestBody AnnonceUpdateDTO dto,
                                                     Authentication authentication) {
        Long userId = extractUserId(authentication);
        AnnonceDTO updated = annonceService.updateAnnonce(id, dto, userId);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH /api/annonces/{id} — Partial update (requires authentication)
     */
    @PatchMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Partially update an announcement (PATCH)", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement patched"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author or insufficient role"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Business conflict or optimistic lock")
    })
    public ResponseEntity<AnnonceDTO> patchAnnonce(@PathVariable Long id,
                                                    @Valid @RequestBody AnnoncePatchDTO dto,
                                                    Authentication authentication) {
        Long userId = extractUserId(authentication);
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        AnnonceDTO patched = annonceService.patchAnnonce(id, dto, userId, isAdmin);
        return ResponseEntity.ok(patched);
    }

    /**
     * DELETE /api/annonces/{id} — Delete announcement (requires authentication)
     */
    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete an announcement", responses = {
            @ApiResponse(responseCode = "204", description = "Announcement deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Not archived")
    })
    public ResponseEntity<Void> deleteAnnonce(@PathVariable Long id, Authentication authentication) {
        Long userId = extractUserId(authentication);
        annonceService.deleteAnnonce(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Extract the authenticated user's ID from the Authentication object.
     */
    private Long extractUserId(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.getUserId();
        }
        throw new org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.BusinessException(
                "UNAUTHORIZED", "Identité non trouvée dans le contexte de sécurité", 401);
    }
}
