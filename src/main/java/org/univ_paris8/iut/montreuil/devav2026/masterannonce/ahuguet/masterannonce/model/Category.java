package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

/**
 * JPA Entity representing a category.
 */
@Entity
@Table(name = "category")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le label ne peut pas être vide")
    @Column(unique = true, nullable = false)
    private String label;

    public Category() {}

    public Category(String label) {
        this.label = label;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    @Override
    public String toString() { return label; }
}
