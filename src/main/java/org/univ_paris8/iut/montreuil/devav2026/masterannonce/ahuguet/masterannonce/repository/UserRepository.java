package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;

import java.util.Optional;

/**
 * Spring Data JPA Repository for User.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
}
