package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnnonceIntegrationTest {

    private AnnonceService service;

    @BeforeEach
    void setUp() {
        service = new AnnonceService();
    }

    @Test
    void testLazyLoadingProblem() {
        service.createAnnonce(new Annonce("Test Lazy", "Desc", "Paris", "m@m.fr"), 1L, 1L);

        List<Annonce> list = service.getAnnonces("Test Lazy", null, null, 1);
        Annonce a = list.get(0);

        assertDoesNotThrow(() -> {
            String label = a.getCategory().getLabel();
            System.out.println("Catégorie chargée : " + label);
        });
    }
}