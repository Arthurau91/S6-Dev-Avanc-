package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for full update of an Annonce (PUT).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body for fully updating an announcement")
public class AnnonceUpdateDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 64, message = "Le titre ne doit pas dépasser 64 caractères")
    @Schema(description = "Title", example = "Vends PC portable", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 256, message = "La description ne doit pas dépasser 256 caractères")
    @Schema(description = "Description", example = "PC en excellent état", requiredMode = Schema.RequiredMode.REQUIRED)
    private String description;

    @Size(max = 64, message = "L'adresse ne doit pas dépasser 64 caractères")
    @Schema(description = "Address", example = "123 Rue de Montreuil")
    private String adress;

    @Size(max = 64, message = "L'email ne doit pas dépasser 64 caractères")
    @Schema(description = "Contact email", example = "user@example.com")
    private String mail;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @NotNull(message = "La version est obligatoire pour le verrouillage optimiste")
    @Schema(description = "Version for optimistic locking", example = "0", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long version;
}
