package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.junit.jupiter.api.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnnonceRepository using H2 in-memory database.
 */
class AnnonceRepositoryIntegrationTest {

    private static AnnonceRepository annonceRepository;
    private static UserRepository userRepository;
    private static CategoryRepository categoryRepository;

    private static User testUser;
    private static Category testCategory;

    @BeforeAll
    static void setUpAll() {
        EntityManagerHelper.init("MasterAnnonceTestPU");
        annonceRepository = new AnnonceRepository();
        userRepository = new UserRepository();
        categoryRepository = new CategoryRepository();

        // Load test dataset
        loadTestData();
    }

    @AfterAll
    static void tearDownAll() {
        EntityManagerHelper.closeEntityManagerFactory();
    }

    @BeforeEach
    void setUp() {
        // Each test starts fresh with the loaded data
    }

    @AfterEach
    void tearDown() {
        EntityManagerHelper.closeEntityManager();
    }

    static void loadTestData() {
        try {
            EntityManagerHelper.beginTransaction();
            EntityManager em = EntityManagerHelper.getEntityManager();

            // Create test user
            testUser = new User("repotest", "repotest@test.com", "password123", "USER");
            em.persist(testUser);

            // Create test category
            testCategory = new Category("Test Category");
            em.persist(testCategory);

            // Create test announcements
            for (int i = 1; i <= 15; i++) {
                Annonce a = new Annonce("Annonce " + i, "Description " + i, "Adresse " + i, "mail" + i + "@test.com");
                a.setAuthor(testUser);
                a.setCategory(testCategory);
                if (i <= 5) a.setStatus(AnnonceStatus.DRAFT);
                else if (i <= 10) a.setStatus(AnnonceStatus.PUBLISHED);
                else a.setStatus(AnnonceStatus.ARCHIVED);
                em.persist(a);
            }

            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            throw new RuntimeException("Failed to load test data", e);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    @Test
    @DisplayName("findWithFilters - should return paginated results")
    void testPagination() {
        List<Annonce> page1 = annonceRepository.findWithFilters(null, null, null, 1, 5);
        assertEquals(5, page1.size());

        List<Annonce> page2 = annonceRepository.findWithFilters(null, null, null, 2, 5);
        assertEquals(5, page2.size());

        List<Annonce> page3 = annonceRepository.findWithFilters(null, null, null, 3, 5);
        assertEquals(5, page3.size());

        // Page 4 should be empty
        List<Annonce> page4 = annonceRepository.findWithFilters(null, null, null, 4, 5);
        assertEquals(0, page4.size());
    }

    @Test
    @DisplayName("findWithFilters - should filter by status")
    void testFilterByStatus() {
        List<Annonce> drafts = annonceRepository.findWithFilters(null, null, AnnonceStatus.DRAFT, 1, 20);
        assertEquals(5, drafts.size());
        drafts.forEach(a -> assertEquals(AnnonceStatus.DRAFT, a.getStatus()));
    }

    @Test
    @DisplayName("findWithFilters - should filter by keyword")
    void testFilterByKeyword() {
        List<Annonce> results = annonceRepository.findWithFilters("Annonce 1", null, null, 1, 20);
        assertTrue(results.size() >= 1);
        results.forEach(a -> assertTrue(
                a.getTitle().toLowerCase().contains("annonce 1") ||
                a.getDescription().toLowerCase().contains("annonce 1")
        ));
    }

    @Test
    @DisplayName("countWithFilters - should count total results")
    void testCountWithFilters() {
        long total = annonceRepository.countWithFilters(null, null, null);
        assertEquals(15, total);

        long published = annonceRepository.countWithFilters(null, null, AnnonceStatus.PUBLISHED);
        assertEquals(5, published);
    }

    @Test
    @DisplayName("findById - should return annonce")
    void testFindById() {
        List<Annonce> all = annonceRepository.findWithFilters(null, null, null, 1, 1);
        assertFalse(all.isEmpty());

        Annonce found = annonceRepository.findById(all.get(0).getId());
        assertNotNull(found);
        assertEquals(all.get(0).getTitle(), found.getTitle());
    }

    @Test
    @DisplayName("findByIdFetched - should eagerly load author and category")
    void testFindByIdFetched() {
        List<Annonce> all = annonceRepository.findWithFilters(null, null, null, 1, 1);
        assertFalse(all.isEmpty());

        Annonce fetched = annonceRepository.findByIdFetched(all.get(0).getId());
        assertNotNull(fetched);
        assertNotNull(fetched.getAuthor());
        assertNotNull(fetched.getCategory());
        assertEquals("repotest", fetched.getAuthor().getUsername());
    }

    @Test
    @DisplayName("save - should persist new annonce")
    void testSaveNew() {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce newAnnonce = new Annonce("New Test", "New Description", "New Adresse", "new@test.com");
            newAnnonce.setAuthor(EntityManagerHelper.getEntityManager().merge(testUser));
            newAnnonce.setCategory(EntityManagerHelper.getEntityManager().merge(testCategory));
            annonceRepository.save(newAnnonce);

            EntityManagerHelper.commit();

            assertNotNull(newAnnonce.getId());
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            throw e;
        }
    }

    @Test
    @DisplayName("delete - should remove annonce")
    void testDelete() {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce toDelete = new Annonce("To Delete", "Delete me", null, null);
            toDelete.setAuthor(EntityManagerHelper.getEntityManager().merge(testUser));
            annonceRepository.save(toDelete);
            EntityManagerHelper.commit();

            Long id = toDelete.getId();
            assertNotNull(id);

            EntityManagerHelper.closeEntityManager();
            EntityManagerHelper.beginTransaction();
            annonceRepository.delete(id);
            EntityManagerHelper.commit();

            EntityManagerHelper.closeEntityManager();
            Annonce deleted = annonceRepository.findById(id);
            assertNull(deleted);
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            throw e;
        }
    }
}
