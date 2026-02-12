package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class AnnonceRepository {

    public void save(Annonce annonce) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        if (annonce.getId() == null) {
            em.persist(annonce);
        } else {
            em.merge(annonce);
        }
    }

    public Annonce findById(Long id) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        return em.find(Annonce.class, id);
    }

    public void delete(Long id) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        Annonce annonce = em.find(Annonce.class, id);
        if (annonce != null) {
            em.remove(annonce);
        }
    }

    /**
     * Recherche avancée avec filtres et pagination
     */
    public List<Annonce> findWithFilters(String keyword, Long categoryId, AnnonceStatus status, int page, int pageSize) {
        EntityManager em = EntityManagerHelper.getEntityManager();

        StringBuilder jpql = new StringBuilder("SELECT a FROM Annonce a LEFT JOIN FETCH a.category LEFT JOIN FETCH a.author WHERE 1=1 ");

        if (keyword != null && !keyword.isEmpty()) {
            jpql.append("AND (LOWER(a.title) LIKE :keyword OR LOWER(a.description) LIKE :keyword) ");
        }
        if (categoryId != null) {
            jpql.append("AND a.category.id = :catId ");
        }
        if (status != null) {
            jpql.append("AND a.status = :status ");
        }

        jpql.append("ORDER BY a.date DESC");

        TypedQuery<Annonce> query = em.createQuery(jpql.toString(), Annonce.class);

        if (keyword != null && !keyword.isEmpty()) {
            query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        }
        if (categoryId != null) {
            query.setParameter("catId", categoryId);
        }
        if (status != null) {
            query.setParameter("status", status);
        }

        query.setFirstResult((page - 1) * pageSize);
        query.setMaxResults(pageSize);

        return query.getResultList();
    }

    public long countWithFilters(String keyword, Long categoryId, AnnonceStatus status) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        StringBuilder jpql = new StringBuilder("SELECT COUNT(a) FROM Annonce a WHERE 1=1 ");

        if (keyword != null && !keyword.isEmpty()) jpql.append("AND (LOWER(a.title) LIKE :keyword OR LOWER(a.description) LIKE :keyword) ");
        if (categoryId != null) jpql.append("AND a.category.id = :catId ");
        if (status != null) jpql.append("AND a.status = :status ");

        TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);

        if (keyword != null && !keyword.isEmpty()) query.setParameter("keyword", "%" + keyword.toLowerCase() + "%");
        if (categoryId != null) query.setParameter("catId", categoryId);
        if (status != null) query.setParameter("status", status);

        return query.getSingleResult();
    }

    public Annonce findByIdFetched(Long id) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        try {
            String jpql = "SELECT a FROM Annonce a " +
                    "LEFT JOIN FETCH a.category " +
                    "LEFT JOIN FETCH a.author " +
                    "WHERE a.id = :id";
            TypedQuery<Annonce> query = em.createQuery(jpql, Annonce.class);
            query.setParameter("id", id);
            return query.getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}