package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login response containing the JWT token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login response with JWT token")
public class LoginResponseDTO {

    @Schema(description = "JWT authentication token")
    private String token;

    @Schema(description = "Token expiry time in seconds", example = "3600")
    private long expiresIn;
}
