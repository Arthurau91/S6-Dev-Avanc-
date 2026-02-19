package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.Secured;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AnnonceService;

import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.net.URI;

/**
 * REST resource for Annonce CRUD operations.
 * Base path: /api/annonces
 */
@Path("/annonces")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Annonces", description = "CRUD operations for announcements")
public class AnnonceResource {

    private final AnnonceService annonceService;

    public AnnonceResource() {
        this.annonceService = new AnnonceService();
    }

    public AnnonceResource(AnnonceService annonceService) {
        this.annonceService = annonceService;
    }

    /**
     * GET /api/annonces — Paginated list
     */
    @GET
    @Operation(summary = "List announcements (paginated)", responses = {
            @ApiResponse(responseCode = "200", description = "Paginated list of announcements")
    })
    public Response listAnnonces(
            @Parameter(description = "Search keyword") @QueryParam("keyword") String keyword,
            @Parameter(description = "Filter by category ID") @QueryParam("categoryId") Long categoryId,
            @Parameter(description = "Filter by status") @QueryParam("status") String status,
            @Parameter(description = "Page number (1-based)") @QueryParam("page") @DefaultValue("1") int page,
            @Parameter(description = "Page size") @QueryParam("pageSize") @DefaultValue("10") int pageSize) {

        PaginatedResponseDTO<AnnonceDTO> result = annonceService.getAnnonces(keyword, categoryId, status, page, pageSize);
        return Response.ok(result).build();
    }

    /**
     * GET /api/annonces/{id} — Detail view
     */
    @GET
    @Path("/{id}")
    @Operation(summary = "Get announcement by ID", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement found"),
            @ApiResponse(responseCode = "404", description = "Announcement not found")
    })
    public Response getAnnonce(@Parameter(description = "Announcement ID") @PathParam("id") Long id) {
        AnnonceDTO dto = annonceService.getAnnonceById(id);
        return Response.ok(dto).build();
    }

    /**
     * POST /api/annonces — Create announcement (requires authentication)
     */
    @POST
    @Secured
    @Operation(summary = "Create a new announcement", responses = {
            @ApiResponse(responseCode = "201", description = "Announcement created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public Response createAnnonce(@Valid AnnonceCreateDTO dto, @Context SecurityContext securityContext) {
        Long userId = extractUserId(securityContext);
        AnnonceDTO created = annonceService.createAnnonce(dto, userId);
        return Response.created(URI.create("/api/annonces/" + created.getId()))
                .entity(created)
                .build();
    }

    /**
     * PUT /api/annonces/{id} — Full update (requires authentication)
     */
    @PUT
    @Path("/{id}")
    @Secured
    @Operation(summary = "Update an announcement (full)", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Business conflict or optimistic lock")
    })
    public Response updateAnnonce(@PathParam("id") Long id, @Valid AnnonceUpdateDTO dto,
                                   @Context SecurityContext securityContext) {
        Long userId = extractUserId(securityContext);
        AnnonceDTO updated = annonceService.updateAnnonce(id, dto, userId);
        return Response.ok(updated).build();
    }

    /**
     * PATCH /api/annonces/{id} — Partial update (requires authentication)
     *
     * Expected logic:
     * - Only non-null fields in the request body are applied to the entity.
     * - The version field is mandatory for optimistic locking.
     * - Status changes can be included (e.g., DRAFT -> PUBLISHED -> ARCHIVED).
     * - Business rules still apply: ownership check, PUBLISHED blocks content modification.
     */
    @PATCH
    @Path("/{id}")
    @Secured
    @Operation(summary = "Partially update an announcement (PATCH)", responses = {
            @ApiResponse(responseCode = "200", description = "Announcement patched"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Business conflict or optimistic lock")
    })
    public Response patchAnnonce(@PathParam("id") Long id, @Valid AnnoncePatchDTO dto,
                                  @Context SecurityContext securityContext) {
        Long userId = extractUserId(securityContext);
        AnnonceDTO patched = annonceService.patchAnnonce(id, dto, userId);
        return Response.ok(patched).build();
    }

    /**
     * DELETE /api/annonces/{id} — Delete announcement (requires authentication)
     */
    @DELETE
    @Path("/{id}")
    @Secured
    @Operation(summary = "Delete an announcement", responses = {
            @ApiResponse(responseCode = "204", description = "Announcement deleted"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Not the author"),
            @ApiResponse(responseCode = "404", description = "Not found"),
            @ApiResponse(responseCode = "409", description = "Not archived")
    })
    public Response deleteAnnonce(@PathParam("id") Long id, @Context SecurityContext securityContext) {
        Long userId = extractUserId(securityContext);
        annonceService.deleteAnnonce(id, userId);
        return Response.noContent().build();
    }

    /**
     * Extract the authenticated user's ID from the SecurityContext.
     * Jersey injects a proxy SecurityContext, so we check the Principal type
     * rather than the SecurityContext wrapper type.
     */
    private Long extractUserId(SecurityContext securityContext) {
        if (securityContext != null && securityContext.getUserPrincipal() instanceof UserPrincipal) {
            return ((UserPrincipal) securityContext.getUserPrincipal()).getUserId();
        }
        throw new javax.ws.rs.NotAuthorizedException("Identity not found in security context");
    }
}
