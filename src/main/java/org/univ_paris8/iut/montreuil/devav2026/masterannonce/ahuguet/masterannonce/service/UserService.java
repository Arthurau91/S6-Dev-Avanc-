package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dao.UserRepository;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.LoginCallbackHandler;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.TokenStore;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.RolePrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import java.util.Set;
import java.util.UUID;

/**
 * Service handling user authentication via JAAS.
 */
public class UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);
    private static final long TOKEN_EXPIRY_SECONDS = 3600; // 1 hour

    private UserRepository userRepository;

    public UserService() {
        this.userRepository = new UserRepository();
    }

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Authenticate a user via JAAS and return a token.
     *
     * JAAS Login Flow:
     * 1. Create LoginContext with "MasterAnnonceLogin" configuration
     * 2. JAAS calls DbLoginModule which verifies credentials against DB
     * 3. On success, extract UserPrincipal and RolePrincipal from Subject
     * 4. Generate UUID token, store in TokenStore
     * 5. Return token + expiresIn
     *
     * @return String array: [token, expiresIn] or null if authentication failed
     */
    public String[] login(String username, String password) throws LoginException {
        LoginContext loginContext = new LoginContext(
                "MasterAnnonceLogin",
                new LoginCallbackHandler(username, password)
        );

        loginContext.login();

        Subject subject = loginContext.getSubject();
        Set<UserPrincipal> userPrincipals = subject.getPrincipals(UserPrincipal.class);
        Set<RolePrincipal> rolePrincipals = subject.getPrincipals(RolePrincipal.class);

        if (userPrincipals.isEmpty()) {
            throw new LoginException("Aucun UserPrincipal dans le Subject JAAS");
        }

        UserPrincipal up = userPrincipals.iterator().next();
        String role = rolePrincipals.isEmpty() ? "USER" : rolePrincipals.iterator().next().getName();

        // Generate token
        String token = UUID.randomUUID().toString();
        TokenStore.store(token, up.getUserId(), up.getName(), role, TOKEN_EXPIRY_SECONDS);

        LOG.info("User '{}' logged in, token generated", username);
        return new String[]{token, String.valueOf(TOKEN_EXPIRY_SECONDS)};
    }

    public User findByUsername(String username) {
        try {
            return userRepository.findByUsername(username);
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }

    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}