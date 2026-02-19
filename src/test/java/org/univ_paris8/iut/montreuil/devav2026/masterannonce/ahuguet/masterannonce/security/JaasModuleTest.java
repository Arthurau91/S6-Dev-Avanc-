package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.module.TokenLoginModule;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.RolePrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginException;
import java.util.HashMap;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JAAS LoginModules.
 * Tests TokenLoginModule directly (no JAAS config needed).
 * Tests TokenStore token lifecycle.
 */
class JaasModuleTest {

    @BeforeEach
    void setUp() {
        TokenStore.clear();
    }

    // --- TokenStore Tests ---

    @Test
    @DisplayName("TokenStore - should store and validate token")
    void testTokenStore_storeAndValidate() {
        TokenStore.store("test-token", 1L, "testuser", "USER", 3600);

        TokenStore.TokenInfo info = TokenStore.validate("test-token");
        assertNotNull(info);
        assertEquals(1L, info.getUserId());
        assertEquals("testuser", info.getUsername());
        assertEquals("USER", info.getRole());
    }

    @Test
    @DisplayName("TokenStore - should return null for unknown token")
    void testTokenStore_unknownToken() {
        TokenStore.TokenInfo info = TokenStore.validate("unknown-token");
        assertNull(info);
    }

    @Test
    @DisplayName("TokenStore - should expire token")
    void testTokenStore_expiredToken() {
        // Store token that expires immediately (0 seconds)
        TokenStore.store("expired-token", 1L, "testuser", "USER", 0);

        // Small delay to ensure expiry
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}

        TokenStore.TokenInfo info = TokenStore.validate("expired-token");
        assertNull(info);
    }

    @Test
    @DisplayName("TokenStore - should remove token")
    void testTokenStore_remove() {
        TokenStore.store("remove-me", 1L, "testuser", "USER", 3600);
        TokenStore.remove("remove-me");

        TokenStore.TokenInfo info = TokenStore.validate("remove-me");
        assertNull(info);
    }

    // --- TokenLoginModule Tests ---

    @Test
    @DisplayName("TokenLoginModule - should authenticate with valid token")
    void testTokenLoginModule_validToken() throws LoginException {
        TokenStore.store("valid-token", 1L, "testuser", "USER", 3600);

        Subject subject = new Subject();
        TokenLoginModule module = new TokenLoginModule();
        module.initialize(subject, new TokenCallbackHandler("valid-token"), new HashMap<>(), new HashMap<>());

        assertTrue(module.login());
        assertTrue(module.commit());

        Set<UserPrincipal> userPrincipals = subject.getPrincipals(UserPrincipal.class);
        assertEquals(1, userPrincipals.size());
        assertEquals("testuser", userPrincipals.iterator().next().getName());

        Set<RolePrincipal> rolePrincipals = subject.getPrincipals(RolePrincipal.class);
        assertEquals(1, rolePrincipals.size());
        assertEquals("USER", rolePrincipals.iterator().next().getName());
    }

    @Test
    @DisplayName("TokenLoginModule - should fail with invalid token")
    void testTokenLoginModule_invalidToken() {
        Subject subject = new Subject();
        TokenLoginModule module = new TokenLoginModule();
        module.initialize(subject, new TokenCallbackHandler("invalid-token"), new HashMap<>(), new HashMap<>());

        assertThrows(LoginException.class, module::login);
    }

    @Test
    @DisplayName("TokenLoginModule - logout should clear principals")
    void testTokenLoginModule_logout() throws LoginException {
        TokenStore.store("logout-token", 1L, "testuser", "USER", 3600);

        Subject subject = new Subject();
        TokenLoginModule module = new TokenLoginModule();
        module.initialize(subject, new TokenCallbackHandler("logout-token"), new HashMap<>(), new HashMap<>());

        module.login();
        module.commit();
        assertEquals(2, subject.getPrincipals().size()); // UserPrincipal + RolePrincipal

        module.logout();
        assertEquals(0, subject.getPrincipals().size());
    }
}
