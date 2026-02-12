package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnonceRepositoryTest {

    private AnnonceRepository repository;

    @BeforeEach
    void setUp() {
        repository = new AnnonceRepository();
        EntityManagerHelper.beginTransaction();
    }

    @AfterEach
    void tearDown() {
        EntityManagerHelper.rollback();
        EntityManagerHelper.closeEntityManager();
    }

    @Test
    void testSaveAndFind() {
        Annonce a = new Annonce("Velo Test", "Description Test", "Paris", "test@mail.com");
        a.setStatus(AnnonceStatus.DRAFT);

        repository.save(a);
        Annonce found = repository.findById(a.getId());

        assertNotNull(found);
        assertEquals("Velo Test", found.getTitle());
    }

    @Test
    void testFindWithFiltersAndPagination() {
        for (int i = 0; i < 3; i++) {
            Annonce a = new Annonce("PC Gamer " + i, "Un super PC", "Paris", "a@a.com");
            a.setStatus(AnnonceStatus.PUBLISHED);
            repository.save(a);
        }

        List<Annonce> results = repository.findWithFilters("Gamer", null, AnnonceStatus.PUBLISHED, 1, 10);

        assertTrue(results.size() >= 3);
        assertEquals("PC Gamer 0", results.get(2).getTitle());
    }
}