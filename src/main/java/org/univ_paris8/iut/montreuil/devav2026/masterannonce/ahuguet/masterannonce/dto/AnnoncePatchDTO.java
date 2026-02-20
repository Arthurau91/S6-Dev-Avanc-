package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for partial update of an Annonce (PATCH).
 * Only non-null fields will be applied.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for partial update (PATCH) of an announcement")
public class AnnoncePatchDTO {

    @Size(max = 64, message = "Le titre ne doit pas dépasser 64 caractères")
    @Schema(description = "Title (optional)", example = "Nouveau titre")
    private String title;

    @Size(max = 256, message = "La description ne doit pas dépasser 256 caractères")
    @Schema(description = "Description (optional)", example = "Nouvelle description")
    private String description;

    @Size(max = 64, message = "L'adresse ne doit pas dépasser 64 caractères")
    @Schema(description = "Address (optional)", example = "456 Avenue de Paris")
    private String adress;

    @Size(max = 64, message = "L'email ne doit pas dépasser 64 caractères")
    @Schema(description = "Contact email (optional)", example = "new@example.com")
    private String mail;

    @Schema(description = "Category ID (optional)", example = "2")
    private Long categoryId;

    @Schema(description = "Status (optional)", example = "PUBLISHED")
    private String status;

    @NotNull(message = "La version est obligatoire pour le verrouillage optimiste")
    @Schema(description = "Version for optimistic locking", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;
}
