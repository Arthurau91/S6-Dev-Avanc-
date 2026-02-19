package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.NotBlank;

/**
 * DTO for login request.
 */
@Schema(description = "Login request")
public class LoginRequestDTO {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Schema(description = "Username", example = "testuser", required = true)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Password", example = "password123", required = true)
    private String password;

    public LoginRequestDTO() {}

    public LoginRequestDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
