package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.AnnonceRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import java.util.List;

public class AnnonceService {

    private AnnonceRepository annonceRepository = new AnnonceRepository();
    private CategoryRepository categoryRepository = new CategoryRepository();
    private UserRepository userRepository = new UserRepository();

    /**
     * Création d'une annonce (Transactionnel)
     */
    public void createAnnonce(Annonce annonce, Long categoryId, Long userId) {
        try {
            EntityManagerHelper.beginTransaction();

            Category cat = categoryRepository.findById(categoryId);
            User author = userRepository.findById(userId);

            annonce.setCategory(cat);
            annonce.setAuthor(author);

            annonce.setStatus(AnnonceStatus.DRAFT);

            annonceRepository.save(annonce);

            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            e.printStackTrace();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Modification d'une annonce
     */
    public void updateAnnonce(Long id, Annonce nouveauxChamps, Long categoryId) {
        try {
            EntityManagerHelper.beginTransaction();

            Annonce annonceExistante = annonceRepository.findById(id);

            if (annonceExistante != null) {
                annonceExistante.setTitle(nouveauxChamps.getTitle());
                annonceExistante.setDescription(nouveauxChamps.getDescription());
                annonceExistante.setAdress(nouveauxChamps.getAdress());
                annonceExistante.setMail(nouveauxChamps.getMail());

                if (categoryId != null) {
                    Category cat = categoryRepository.findById(categoryId);
                    annonceExistante.setCategory(cat);
                }

                annonceRepository.save(annonceExistante);
            }

            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            e.printStackTrace();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Suppression
     */
    public void deleteAnnonce(Long id) {
        try {
            EntityManagerHelper.beginTransaction();
            annonceRepository.delete(id);
            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            e.printStackTrace();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Changement de statut (Workflow : Publish / Archive)
     */
    public void changeStatus(Long id, AnnonceStatus newStatus) {
        try {
            EntityManagerHelper.beginTransaction();
            Annonce a = annonceRepository.findById(id);
            if (a != null) {
                a.setStatus(newStatus);
            }
            EntityManagerHelper.commit();
        } catch (Exception e) {
            EntityManagerHelper.rollback();
            e.printStackTrace();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Lecture : Liste paginée et filtrée
     * Pas de transaction nécessaire pour la lecture seule, mais on gère l'EM.
     */
    public List<Annonce> getAnnonces(String keyword, Long categoryId, AnnonceStatus status, int page) {
        try {
            return annonceRepository.findWithFilters(keyword, categoryId, status, page, 10);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    public Annonce getAnnonce(Long id) {
        try {
            return annonceRepository.findByIdFetched(id);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    /**
     * Compter les résultats (pour la pagination)
     */
    public long countAnnonces(String keyword, Long categoryId, AnnonceStatus status) {
        try {
            return annonceRepository.countWithFilters(keyword, categoryId, status);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    public List<Category> getAllCategories() {
        try {
            return categoryRepository.findAll();
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }


    public void setAnnonceRepository(AnnonceRepository repo) { this.annonceRepository = repo; }
    public void setCategoryRepository(CategoryRepository repo) { this.categoryRepository = repo; }
    public void setUserRepository(UserRepository repo) { this.userRepository = repo; }
}