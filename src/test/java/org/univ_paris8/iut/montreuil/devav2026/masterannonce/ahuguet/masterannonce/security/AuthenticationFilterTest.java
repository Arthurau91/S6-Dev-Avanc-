package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.ErrorResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.filter.JaasAuthenticationFilter;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JaasAuthenticationFilter.
 * Tests the filter logic: missing header, invalid token, valid token.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationFilterTest {

    private JaasAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JaasAuthenticationFilter();
        TokenStore.clear();

        // Configure JAAS for tests
        java.net.URL resource = getClass().getClassLoader().getResource("jaas.conf");
        if (resource != null) {
            System.setProperty("java.security.auth.login.config", resource.toExternalForm());
        }
    }

    @Test
    @DisplayName("Filter - should return 401 when no Authorization header")
    void testMissingAuthorizationHeader() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn(null);

        filter.filter(context);

        verify(context).abortWith(argThat(response ->
                response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("Filter - should return 401 when Authorization is not Bearer")
    void testInvalidAuthorizationScheme() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        filter.filter(context);

        verify(context).abortWith(argThat(response ->
                response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("Filter - should return 401 for invalid/expired token")
    void testInvalidToken() {
        ContainerRequestContext context = mock(ContainerRequestContext.class);
        when(context.getHeaderString("Authorization")).thenReturn("Bearer invalid-token-xyz");

        filter.filter(context);

        verify(context).abortWith(argThat(response ->
                response.getStatus() == Response.Status.UNAUTHORIZED.getStatusCode()
        ));
    }

    @Test
    @DisplayName("Filter - should set SecurityContext for valid token")
    void testValidToken() {
        // Store a valid token
        TokenStore.store("valid-filter-token", 1L, "testuser", "USER", 3600);

        ContainerRequestContext context = mock(ContainerRequestContext.class);
        SecurityContext originalSecContext = mock(SecurityContext.class);
        when(context.getHeaderString("Authorization")).thenReturn("Bearer valid-filter-token");
        when(context.getSecurityContext()).thenReturn(originalSecContext);
        when(originalSecContext.isSecure()).thenReturn(false);

        filter.filter(context);

        // Filter should NOT abort — it should set the security context
        verify(context, never()).abortWith(any());
        verify(context).setSecurityContext(argThat(sc -> {
            assertTrue(sc instanceof UserSecurityContext);
            UserSecurityContext usc = (UserSecurityContext) sc;
            assertEquals("testuser", usc.getUserPrincipal().getName());
            return true;
        }));
    }
}
