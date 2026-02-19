package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.sql.Timestamp;

/**
 * Response DTO for Annonce entity.
 * Uses Builder pattern for Entity -> DTO mapping.
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
    private Timestamp date;

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

    private AnnonceDTO() {}

    // --- Getters ---

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getAdress() { return adress; }
    public String getMail() { return mail; }
    public Timestamp getDate() { return date; }
    public String getStatus() { return status; }
    public String getAuthorUsername() { return authorUsername; }
    public Long getAuthorId() { return authorId; }
    public String getCategoryLabel() { return categoryLabel; }
    public Long getCategoryId() { return categoryId; }
    public Long getVersion() { return version; }

    // --- Builder Pattern ---

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final AnnonceDTO dto = new AnnonceDTO();

        public Builder id(Long id) { dto.id = id; return this; }
        public Builder title(String title) { dto.title = title; return this; }
        public Builder description(String description) { dto.description = description; return this; }
        public Builder adress(String adress) { dto.adress = adress; return this; }
        public Builder mail(String mail) { dto.mail = mail; return this; }
        public Builder date(Timestamp date) { dto.date = date; return this; }
        public Builder status(String status) { dto.status = status; return this; }
        public Builder authorUsername(String authorUsername) { dto.authorUsername = authorUsername; return this; }
        public Builder authorId(Long authorId) { dto.authorId = authorId; return this; }
        public Builder categoryLabel(String categoryLabel) { dto.categoryLabel = categoryLabel; return this; }
        public Builder categoryId(Long categoryId) { dto.categoryId = categoryId; return this; }
        public Builder version(Long version) { dto.version = version; return this; }

        public AnnonceDTO build() { return dto; }
    }
}
