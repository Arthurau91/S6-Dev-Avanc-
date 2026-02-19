package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * Application initializer: seeds database with default categories and test user.
 * Also configures JAAS if not set via JVM argument.
 */
@WebListener
public class AppInitializer implements ServletContextListener {

    private static final Logger LOG = LoggerFactory.getLogger(AppInitializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        LOG.info("--- APPLICATION STARTING - INITIALIZATION ---");

        // Configure JAAS if not already set via -Djava.security.auth.login.config
        configureJaas();

        EntityManager em = EntityManagerHelper.getEntityManager();
        UserRepository userRepo = new UserRepository();
        CategoryRepository catRepo = new CategoryRepository();

        try {
            EntityManagerHelper.beginTransaction();

            if (catRepo.findAll().isEmpty()) {
                em.persist(new Category("Informatique"));
                em.persist(new Category("Immobilier"));
                em.persist(new Category("Cours particuliers"));
                em.persist(new Category("Emploi"));
                em.persist(new Category("Services"));
                LOG.info("-> Categories created");
            }

            if (userRepo.findByUsername("admin") == null) {
                User admin = new User("admin", "admin@univ-paris8.fr", "admin123", "ADMIN");
                em.persist(admin);
                LOG.info("-> Admin user created (ID: {})", admin.getId());
            }

            if (userRepo.findByUsername("testuser") == null) {
                User u = new User("testuser", "test@univ-paris8.fr", "password123", "USER");
                em.persist(u);
                LOG.info("-> Test user created (ID: {})", u.getId());
            }

            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            LOG.error("Error during initialization", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        LOG.info("--- APPLICATION SHUTTING DOWN ---");
        EntityManagerHelper.closeEntityManagerFactory();
    }

    private void configureJaas() {
        String jaasConfig = System.getProperty("java.security.auth.login.config");
        if (jaasConfig == null) {
            // Try to find jaas.conf in classpath
            java.net.URL resource = getClass().getClassLoader().getResource("jaas.conf");
            if (resource != null) {
                System.setProperty("java.security.auth.login.config", resource.toExternalForm());
                LOG.info("JAAS configuration loaded from classpath: {}", resource.toExternalForm());
            } else {
                LOG.warn("JAAS configuration file (jaas.conf) not found in classpath. "
                        + "Set -Djava.security.auth.login.config=<path> to specify it.");
            }
        } else {
            LOG.info("JAAS configuration already set: {}", jaasConfig);
        }
    }
}