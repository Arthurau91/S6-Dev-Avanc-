package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Category;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.CategoryRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.repository.UserRepository;

import java.util.List;

/**
 * Seeds database with initial data on startup (if empty).
 * Passwords are BCrypt-hashed for Spring Security.
 */
@Configuration
public class DataInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository,
                                   CategoryRepository categoryRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            // Seed categories
            if (categoryRepository.count() == 0) {
                List<String> labels = List.of("Informatique", "Immobilier", "Cours particuliers", "Emploi", "Services");
                labels.forEach(label -> categoryRepository.save(new Category(label)));
                LOG.info("Seeded {} categories", labels.size());
            }

            // Seed users with BCrypt passwords
            if (userRepository.count() == 0) {
                User admin = new User("admin", "admin@univ-paris8.fr",
                        passwordEncoder.encode("admin123"), "ADMIN");
                userRepository.save(admin);

                User testUser = new User("testuser", "test@univ-paris8.fr",
                        passwordEncoder.encode("password123"), "USER");
                userRepository.save(testUser);

                LOG.info("Seeded 2 users (admin, testuser)");
            }
        };
    }
}
