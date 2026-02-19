package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * DTO for login response containing the JWT token.
 */
@Schema(description = "Login response with JWT token")
public class LoginResponseDTO {

    @Schema(description = "JWT authentication token")
    private String token;

    @Schema(description = "Token expiry time in seconds", example = "3600")
    private long expiresIn;

    public LoginResponseDTO() {}

    public LoginResponseDTO(String token, long expiresIn) {
        this.token = token;
        this.expiresIn = expiresIn;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }
}
