package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for Annonce entity.
 * Never expose JPA entities — always use this DTO in controllers.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Annonce response representation")
public class AnnonceDTO {

    @Schema(description = "Unique identifier", example = "1")
    private Long id;

    @Schema(description = "Title of the announcement", example = "Vends PC portable")
    private String title;

    @Schema(description = "Description", example = "PC en excellent état")
    private String description;

    @Schema(description = "Address", example = "123 Rue de Montreuil")
    private String adress;

    @Schema(description = "Contact email", example = "user@example.com")
    private String mail;

    @Schema(description = "Creation date")
    private LocalDateTime date;

    @Schema(description = "Status", example = "DRAFT")
    private String status;

    @Schema(description = "Author username", example = "testuser")
    private String authorUsername;

    @Schema(description = "Author ID", example = "1")
    private Long authorId;

    @Schema(description = "Category label", example = "Informatique")
    private String categoryLabel;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    @Schema(description = "Version (optimistic locking)", example = "0")
    private Long version;
}
