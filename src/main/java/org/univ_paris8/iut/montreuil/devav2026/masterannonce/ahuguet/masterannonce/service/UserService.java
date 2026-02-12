package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

public class UserService {

    private UserRepository userRepository = new UserRepository();

    public User login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username);

            if (user != null && user.getPassword().equals(password)) {
                return user;
            }
            return null;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }
}