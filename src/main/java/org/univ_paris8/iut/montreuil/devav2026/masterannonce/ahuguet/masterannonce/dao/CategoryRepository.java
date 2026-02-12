package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class CategoryRepository {

    public List<Category> findAll() {
        EntityManager em = EntityManagerHelper.getEntityManager();
        String jpql = "SELECT c FROM Category c ORDER BY c.label";
        TypedQuery<Category> query = em.createQuery(jpql, Category.class);
        return query.getResultList();
    }

    public Category findById(Long id) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        return em.find(Category.class, id);
    }

    public void save(Category category) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        em.persist(category);
    }
}