package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;

import java.security.Principal;

/**
 * Custom JAX-RS SecurityContext backed by JAAS Subject.
 */
public class UserSecurityContext implements javax.ws.rs.core.SecurityContext {

    private final UserPrincipal userPrincipal;
    private final String role;
    private final boolean secure;

    public UserSecurityContext(UserPrincipal userPrincipal, String role, boolean secure) {
        this.userPrincipal = userPrincipal;
        this.role = role;
        this.secure = secure;
    }

    @Override
    public Principal getUserPrincipal() {
        return userPrincipal;
    }

    @Override
    public boolean isUserInRole(String role) {
        return this.role != null && this.role.equals(role);
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public String getAuthenticationScheme() {
        return "Bearer";
    }

    /**
     * Convenience: get the authenticated user's ID.
     */
    public Long getUserId() {
        return userPrincipal != null ? userPrincipal.getUserId() : null;
    }
}
