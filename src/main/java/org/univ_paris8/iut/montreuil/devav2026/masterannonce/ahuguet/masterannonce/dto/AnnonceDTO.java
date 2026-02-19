package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Response DTO for Annonce entity.
 * Never expose JPA entities — always use this DTO in controllers.
 */
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

    public AnnonceDTO() {}

    // --- Getters & Setters ---

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }

    public String getCategoryLabel() { return categoryLabel; }
    public void setCategoryLabel(String categoryLabel) { this.categoryLabel = categoryLabel; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }
}
