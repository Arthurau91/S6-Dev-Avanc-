package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;

/**
 * Spring Data JPA Repository for Category.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}
