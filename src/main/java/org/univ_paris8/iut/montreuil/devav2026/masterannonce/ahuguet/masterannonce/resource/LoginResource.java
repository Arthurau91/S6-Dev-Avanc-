package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.ErrorResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginRequestDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.UserService;

import javax.security.auth.login.LoginException;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST resource for user authentication.
 * POST /api/login
 *
 * JAAS Login Flow:
 * 1. Client sends { "username": "...", "password": "..." }
 * 2. Server authenticates via JAAS (DbLoginModule verifies against UserRepository)
 * 3. On success, a UUID token is generated and stored in memory (TokenStore)
 * 4. Server returns { "token": "...", "expiresIn": 3600 }
 * 5. Client includes the token in subsequent requests: Authorization: Bearer <token>
 */
@Path("/login")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Login and token management")
public class LoginResource {

    private static final Logger LOG = LoggerFactory.getLogger(LoginResource.class);

    private final UserService userService;

    public LoginResource() {
        this.userService = new UserService();
    }

    public LoginResource(UserService userService) {
        this.userService = userService;
    }

    @POST
    @Operation(summary = "Authenticate user and obtain token", responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, token returned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public Response login(@Valid LoginRequestDTO request) {
        try {
            String[] result = userService.login(request.getUsername(), request.getPassword());
            String token = result[0];
            long expiresIn = Long.parseLong(result[1]);

            LoginResponseDTO response = new LoginResponseDTO(token, expiresIn);
            LOG.info("Login successful for user '{}'", request.getUsername());
            return Response.ok(response).build();

        } catch (LoginException e) {
            LOG.warn("Login failed for user '{}': {}", request.getUsername(), e.getMessage());
            ErrorResponseDTO error = new ErrorResponseDTO("UNAUTHORIZED", "Identifiants invalides");
            return Response.status(Response.Status.UNAUTHORIZED).entity(error).build();
        }
    }
}
