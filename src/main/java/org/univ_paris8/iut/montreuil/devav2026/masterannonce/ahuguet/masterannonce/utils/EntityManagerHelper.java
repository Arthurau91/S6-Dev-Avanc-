package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * ThreadLocal-based EntityManager management.
 * Supports configurable persistence unit for test/production environments.
 */
public class EntityManagerHelper {

    private static final Logger LOG = LoggerFactory.getLogger(EntityManagerHelper.class);
    private static final String DEFAULT_PU = "MasterAnnoncePU";
    private static volatile EntityManagerFactory emf;
    private static final ThreadLocal<EntityManager> threadLocal = new ThreadLocal<>();

    static {
        try {
            emf = Persistence.createEntityManagerFactory(DEFAULT_PU);
        } catch (Exception e) {
            LOG.warn("Could not initialize default persistence unit '{}': {}", DEFAULT_PU, e.getMessage());
        }
    }

    /**
     * Initialize with a specific persistence unit (used for tests with H2).
     */
    public static synchronized void init(String persistenceUnitName) {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
        emf = Persistence.createEntityManagerFactory(persistenceUnitName);
        LOG.info("EntityManagerHelper initialized with PU: {}", persistenceUnitName);
    }

    public static EntityManager getEntityManager() {
        EntityManager em = threadLocal.get();
        if (em == null || !em.isOpen()) {
            em = emf.createEntityManager();
            threadLocal.set(em);
        }
        return em;
    }

    public static void closeEntityManager() {
        EntityManager em = threadLocal.get();
        if (em != null) {
            em.close();
            threadLocal.remove();
        }
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    public static void beginTransaction() {
        getEntityManager().getTransaction().begin();
    }

    public static void commit() {
        getEntityManager().getTransaction().commit();
    }

    public static void rollback() {
        try {
            if (getEntityManager().getTransaction().isActive()) {
                getEntityManager().getTransaction().rollback();
            }
        } catch (Exception e) {
            LOG.error("Error during rollback", e);
        }
    }
}