package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Timestamp;
import java.time.Instant;

@Entity
@Table(name = "annonce")
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

    private Timestamp date;

    @Enumerated(EnumType.STRING)
    private AnnonceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Annonce() {
        this.date = Timestamp.from(Instant.now());
        this.status = AnnonceStatus.DRAFT;
    }

    public Annonce(String title, String description, String adress, String mail) {
        this();
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
    }

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

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }

    public AnnonceStatus getStatus() { return status; }
    public void setStatus(AnnonceStatus status) { this.status = status; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}