package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.module;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.TokenStore;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.RolePrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.util.Map;

/**
 * JAAS LoginModule that validates a Bearer token from the in-memory TokenStore.
 * Used during the Request flow (ContainerRequestFilter).
 *
 * Flow:
 * 1. login() - Extract token from NameCallback, validate in TokenStore
 * 2. commit() - If valid, reconstruct UserPrincipal and RolePrincipal, add to Subject
 * 3. abort() / logout() - Clean up principals
 */
public class TokenLoginModule implements LoginModule {

    private static final Logger LOG = LoggerFactory.getLogger(TokenLoginModule.class);

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
        NameCallback nameCallback = new NameCallback("Token");

        try {
            callbackHandler.handle(new Callback[]{nameCallback});
        } catch (IOException | UnsupportedCallbackException e) {
            throw new LoginException("Error handling callbacks: " + e.getMessage());
        }

        String token = nameCallback.getName();
        LOG.debug("TokenLoginModule: validating token");

        TokenStore.TokenInfo tokenInfo = TokenStore.validate(token);
        if (tokenInfo == null) {
            LOG.warn("TokenLoginModule: invalid or expired token");
            throw new LoginException("Token invalide ou expiré");
        }

        // Token is valid — reconstruct identity
        userPrincipal = new UserPrincipal(tokenInfo.getUserId(), tokenInfo.getUsername());
        rolePrincipal = new RolePrincipal(tokenInfo.getRole());
        loginSucceeded = true;

        LOG.debug("TokenLoginModule: token validated for user '{}'", tokenInfo.getUsername());
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
}
