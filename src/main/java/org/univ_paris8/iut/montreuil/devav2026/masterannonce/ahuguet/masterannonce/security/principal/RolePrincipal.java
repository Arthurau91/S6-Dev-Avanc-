package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal;

import java.io.Serializable;
import java.security.Principal;

/**
 * JAAS Principal representing a user's role.
 */
public class RolePrincipal implements Principal, Serializable {

    private static final long serialVersionUID = 1L;

    private final String role;

    public RolePrincipal(String role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return role;
    }

    @Override
    public String toString() {
        return "RolePrincipal{role='" + role + "'}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RolePrincipal that = (RolePrincipal) o;
        return role.equals(that.role);
    }

    @Override
    public int hashCode() {
        return role.hashCode();
    }
}
