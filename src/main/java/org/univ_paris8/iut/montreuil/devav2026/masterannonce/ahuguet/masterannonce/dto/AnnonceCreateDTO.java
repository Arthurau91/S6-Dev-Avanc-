package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * DTO for creating a new Annonce.
 */
@Schema(description = "Request body for creating an announcement")
public class AnnonceCreateDTO {

    @NotBlank(message = "Le titre est obligatoire")
    @Size(max = 64, message = "Le titre ne doit pas dépasser 64 caractères")
    @Schema(description = "Title", example = "Vends PC portable", required = true)
    private String title;

    @NotBlank(message = "La description est obligatoire")
    @Size(max = 256, message = "La description ne doit pas dépasser 256 caractères")
    @Schema(description = "Description", example = "PC en excellent état", required = true)
    private String description;

    @Size(max = 64, message = "L'adresse ne doit pas dépasser 64 caractères")
    @Schema(description = "Address", example = "123 Rue de Montreuil")
    private String adress;

    @Size(max = 64, message = "L'email ne doit pas dépasser 64 caractères")
    @Schema(description = "Contact email", example = "user@example.com")
    private String mail;

    @Schema(description = "Category ID", example = "1")
    private Long categoryId;

    public AnnonceCreateDTO() {}

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
}
