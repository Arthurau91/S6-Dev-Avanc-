package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory token store for stateless authentication.
 * Maps token (UUID string) -> TokenInfo (userId, username, role, expiry).
 */
public class TokenStore {

    private static final Logger LOG = LoggerFactory.getLogger(TokenStore.class);
    private static final ConcurrentHashMap<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public static void store(String token, Long userId, String username, String role, long expiresInSeconds) {
        Instant expiry = Instant.now().plusSeconds(expiresInSeconds);
        tokens.put(token, new TokenInfo(userId, username, role, expiry));
        LOG.info("Token stored for user '{}', expires at {}", username, expiry);
    }

    public static TokenInfo validate(String token) {
        TokenInfo info = tokens.get(token);
        if (info == null) {
            return null;
        }
        if (Instant.now().isAfter(info.getExpiry())) {
            tokens.remove(token);
            LOG.info("Token expired for user '{}'", info.getUsername());
            return null;
        }
        return info;
    }

    public static void remove(String token) {
        tokens.remove(token);
    }

    public static void clear() {
        tokens.clear();
    }

    /**
     * Token metadata.
     */
    public static class TokenInfo {
        private final Long userId;
        private final String username;
        private final String role;
        private final Instant expiry;

        public TokenInfo(Long userId, String username, String role, Instant expiry) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.expiry = expiry;
        }

        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getRole() { return role; }
        public Instant getExpiry() { return expiry; }
    }
}
