package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import java.util.List;

public class UserRepository {

    public void save(User user) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        em.persist(user);
    }

    public User findById(Long id) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        return em.find(User.class, id);
    }

    public User findByUsername(String username) {
        EntityManager em = EntityManagerHelper.getEntityManager();
        String jpql = "SELECT u FROM User u WHERE u.username = :username";
        TypedQuery<User> query = em.createQuery(jpql, User.class);
        query.setParameter("username", username);

        try {
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<User> findAll() {
        EntityManager em = EntityManagerHelper.getEntityManager();
        return em.createQuery("SELECT u FROM User u", User.class).getResultList();
    }
}