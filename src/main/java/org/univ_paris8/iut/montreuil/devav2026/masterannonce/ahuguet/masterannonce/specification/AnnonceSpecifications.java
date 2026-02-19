package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.specification;

import org.springframework.data.jpa.domain.Specification;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;

import java.time.LocalDateTime;

/**
 * JPA Specifications for dynamic Annonce queries.
 * Each method returns a composable Specification — no giant JPQL needed.
 */
public final class AnnonceSpecifications {

    private AnnonceSpecifications() {}

    /**
     * Search keyword in title OR description (case-insensitive).
     */
    public static Specification<Annonce> hasKeyword(String keyword) {
        return (root, query, cb) -> {
            String pattern = "%" + keyword.toLowerCase() + "%";
            return cb.or(
                    cb.like(cb.lower(root.get("title")), pattern),
                    cb.like(cb.lower(root.get("description")), pattern)
            );
        };
    }

    /**
     * Filter by status.
     */
    public static Specification<Annonce> hasStatus(AnnonceStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    /**
     * Filter by category ID.
     */
    public static Specification<Annonce> hasCategoryId(Long categoryId) {
        return (root, query, cb) -> cb.equal(root.get("category").get("id"), categoryId);
    }

    /**
     * Filter by author ID.
     */
    public static Specification<Annonce> hasAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    /**
     * Filter annonces created after a given date.
     */
    public static Specification<Annonce> createdAfter(LocalDateTime fromDate) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("date"), fromDate);
    }

    /**
     * Filter annonces created before a given date.
     */
    public static Specification<Annonce> createdBefore(LocalDateTime toDate) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("date"), toDate);
    }
}
