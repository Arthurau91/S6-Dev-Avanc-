package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.ErrorResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.Secured;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.TokenCallbackHandler;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.UserSecurityContext;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.RolePrincipal;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.principal.UserPrincipal;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.util.Set;

/**
 * JAX-RS ContainerRequestFilter that implements the JAAS Request Flow.
 *
 * Applied only to @Secured endpoints via NameBinding.
 *
 * Flow:
 * 1. Read Authorization: Bearer <token> from request header
 * 2. Trigger JAAS Login with "MasterAnnonceToken" configuration
 * 3. If successful, extract UserPrincipal and RolePrincipal from Subject
 * 4. Set a custom SecurityContext on the request
 * 5. If unauthorized, return 401
 */
@Secured
@Provider
public class JaasAuthenticationFilter implements ContainerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JaasAuthenticationFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(AUTHORIZATION_HEADER);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            LOG.warn("Missing or invalid Authorization header");
            abortWithUnauthorized(requestContext, "Token d'authentification manquant");
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        try {
            // Trigger JAAS login with "MasterAnnonceToken" config
            LoginContext loginContext = new LoginContext(
                    "MasterAnnonceToken",
                    new TokenCallbackHandler(token)
            );
            loginContext.login();

            // Extract principals from the authenticated Subject
            Subject subject = loginContext.getSubject();
            Set<UserPrincipal> userPrincipals = subject.getPrincipals(UserPrincipal.class);
            Set<RolePrincipal> rolePrincipals = subject.getPrincipals(RolePrincipal.class);

            if (userPrincipals.isEmpty()) {
                abortWithUnauthorized(requestContext, "Identité non trouvée dans le Subject JAAS");
                return;
            }

            UserPrincipal userPrincipal = userPrincipals.iterator().next();
            String role = rolePrincipals.isEmpty() ? "USER" : rolePrincipals.iterator().next().getName();

            // Set custom SecurityContext
            boolean isSecure = requestContext.getSecurityContext().isSecure();
            requestContext.setSecurityContext(new UserSecurityContext(userPrincipal, role, isSecure));

            // Store Subject in request properties for downstream access
            requestContext.setProperty("javax.security.auth.Subject", subject);

            LOG.debug("Authenticated user '{}' with role '{}'", userPrincipal.getName(), role);

        } catch (LoginException e) {
            LOG.warn("JAAS token authentication failed: {}", e.getMessage());
            abortWithUnauthorized(requestContext, "Token invalide ou expiré");
        }
    }

    private void abortWithUnauthorized(ContainerRequestContext requestContext, String message) {
        ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHORIZED", message);
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(error)
                        .type(MediaType.APPLICATION_JSON)
                        .build()
        );
    }
}
