package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * JPA Entity representing an announcement.
 */
@Entity
@Table(name = "annonce", indexes = {
        @Index(name = "idx_annonce_status", columnList = "status"),
        @Index(name = "idx_annonce_author", columnList = "author_id"),
        @Index(name = "idx_annonce_category", columnList = "category_id")
})
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 64, message = "Le titre est trop long")
    @Column(length = 64, nullable = false)
    private String title;

    @NotBlank
    @Size(max = 256, message = "La description est trop longue")
    @Column(length = 256)
    private String description;

    @Size(max = 64)
    @Column(length = 64)
    private String adress;

    @Size(max = 64)
    @Column(length = 64)
    private String mail;

    @Column(name = "date")
    private LocalDateTime date;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private AnnonceStatus status;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Annonce() {
        this.date = LocalDateTime.now();
        this.status = AnnonceStatus.DRAFT;
    }

    public Annonce(String title, String description, String adress, String mail) {
        this();
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
    }

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

    public AnnonceStatus getStatus() { return status; }
    public void setStatus(AnnonceStatus status) { this.status = status; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
