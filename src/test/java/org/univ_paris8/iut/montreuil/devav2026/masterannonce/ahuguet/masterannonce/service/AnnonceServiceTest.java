package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnnonceServiceTest {

    @Mock
    private AnnonceRepository annonceRepo;
    @Mock
    private CategoryRepository categoryRepo;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private AnnonceService service;

    @Test
    void testCreateAnnonce() {
        Long catId = 1L;
        Long userId = 10L;
        Annonce a = new Annonce("Titre", "Desc", "Adr", "Mail");

        when(userRepo.findById(userId)).thenReturn(new User("Auth", "m@m.fr", "pass"));
        when(categoryRepo.findById(catId)).thenReturn(new Category("CatTest"));

        service.createAnnonce(a, catId, userId);

        assertEquals(AnnonceStatus.DRAFT, a.getStatus());

        verify(annonceRepo).save(a);
    }
}