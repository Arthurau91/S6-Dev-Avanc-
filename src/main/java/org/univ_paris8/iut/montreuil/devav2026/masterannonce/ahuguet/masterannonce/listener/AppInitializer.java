package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.listener;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.persistence.EntityManager;

@WebListener
public class AppInitializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("--- DÉMARRAGE DE L'APPLICATION - INITIALISATION ---");

        EntityManager em = EntityManagerHelper.getEntityManager();
        UserRepository userRepo = new UserRepository();
        CategoryRepository catRepo = new CategoryRepository();

        try {
            EntityManagerHelper.beginTransaction();

            if (catRepo.findAll().isEmpty()) {
                em.persist(new Category("Informatique"));
                em.persist(new Category("Immobilier"));
                em.persist(new Category("Cours particuliers"));
                System.out.println("-> Catégories créées.");
            }

            if (userRepo.findByUsername("testuser") == null) {
                User u = new User("testuser", "test@univ-paris8.fr", "password123");
                em.persist(u);
                System.out.println("-> User 'testuser' créé (ID: " + u.getId() + ")");
            }

            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            e.printStackTrace();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        EntityManagerHelper.closeEntityManagerFactory();
    }
}