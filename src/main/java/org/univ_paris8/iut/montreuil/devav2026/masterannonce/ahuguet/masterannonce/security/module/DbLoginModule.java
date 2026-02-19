package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.User;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.RolePrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.utils.EntityManagerHelper;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

/**
 * JAAS LoginModule that verifies username/password against the database.
 * Used during the Login flow (POST /api/login).
 *
 * Flow:
 * 1. login() - Extract credentials from callbacks, verify against DB
 * 2. commit() - If authentication succeeded, add principals to Subject
 * 3. abort() - Clean up on failure
 * 4. logout() - Remove principals from Subject
 */
public class DbLoginModule implements LoginModule {

    private static final Logger LOG = LoggerFactory.getLogger(DbLoginModule.class);

    private Subject subject;
    private CallbackHandler callbackHandler;

    private boolean loginSucceeded = false;
    private boolean commitSucceeded = false;

    private UserPrincipal userPrincipal;
    private RolePrincipal rolePrincipal;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }

    @Override
    public boolean login() throws LoginException {
        NameCallback nameCallback = new NameCallback("Username");
        PasswordCallback passwordCallback = new PasswordCallback("Password", false);

        try {
            callbackHandler.handle(new Callback[]{nameCallback, passwordCallback});
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Error handling callbacks: " + e.getMessage());
        }

        String username = nameCallback.getName();
        String password = new String(passwordCallback.getPassword());
        passwordCallback.clearPassword();

        LOG.debug("DbLoginModule: attempting login for user '{}'", username);

        // Look up user in database
        User user = findUserByUsername(username);
        if (user == null || !user.getPassword().equals(password)) {
            LOG.warn("DbLoginModule: authentication failed for user '{}'", username);
            throw new LoginException("Identifiants invalides");
        }

        // Authentication succeeded
        userPrincipal = new UserPrincipal(user.getId(), user.getUsername());
        rolePrincipal = new RolePrincipal(user.getRole());
        loginSucceeded = true;

        LOG.info("DbLoginModule: user '{}' authenticated successfully", username);
        return true;
    }

    @Override
    public boolean commit() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }
        subject.getPrincipals().add(userPrincipal);
        subject.getPrincipals().add(rolePrincipal);
        commitSucceeded = true;
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if (!loginSucceeded) {
            return false;
        }
        if (commitSucceeded) {
            logout();
        }
        userPrincipal = null;
        rolePrincipal = null;
        loginSucceeded = false;
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        subject.getPrincipals().remove(userPrincipal);
        subject.getPrincipals().remove(rolePrincipal);
        commitSucceeded = false;
        loginSucceeded = false;
        userPrincipal = null;
        rolePrincipal = null;
        return true;
    }

    /**
     * Look up a user in the database by username.
     */
    private User findUserByUsername(String username) {
        try {
            EntityManager em = EntityManagerHelper.getEntityManager();
            return em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            EntityManagerHelper.closeEntityManager();
        }
    }
}
