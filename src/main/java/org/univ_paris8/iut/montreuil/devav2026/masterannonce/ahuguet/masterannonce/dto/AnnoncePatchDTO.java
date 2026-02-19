package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * DTO for partial update of an Annonce (PATCH).
 * Fields are optional — only non-null fields will be applied.
 *
 * Expected logic:
 * 1. Client sends only the fields to update (null fields are ignored).
 * 2. The version field is mandatory for optimistic locking.
 * 3. The service iterates over non-null fields and applies them to the existing entity.
 * 4. Business rules still apply (e.g. PUBLISHED status blocks modification).
 */
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
    @Schema(description = "Version for optimistic locking", example = "0", required = true)
    private Long version;

    public AnnoncePatchDTO() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
