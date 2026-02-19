package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import java.io.Serial;
import java.security.Principal;

/**
 * Custom principal carrying userId and username.
 * Used in SecurityContext after JWT authentication.
 */
public class UserPrincipal implements Principal {

    @Serial
    private static final long serialVersionUID = 1L;

    private final Long userId;
    private final String username;

    public UserPrincipal(Long userId, String username) {
        this.userId = userId;
        this.username = username;
    }

    public Long getUserId() { return userId; }

    @Override
    public String getName() { return username; }

    @Override
    public String toString() {
        return "UserPrincipal{userId=" + userId + ", username='" + username + "'}";
    }
}
