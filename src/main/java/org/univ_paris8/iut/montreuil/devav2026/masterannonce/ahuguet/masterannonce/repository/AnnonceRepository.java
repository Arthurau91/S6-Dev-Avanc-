package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

/**
 * Spring Data JPA Repository for Annonce.
 * Extends JpaSpecificationExecutor for dynamic Specification-based queries.
 */
@Repository
public interface AnnonceRepository extends JpaRepository<Annonce, Long>, JpaSpecificationExecutor<Annonce> {
}
