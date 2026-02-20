package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Login request")
public class LoginRequestDTO {

    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Schema(description = "Username", example = "testuser", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Schema(description = "Password", example = "password123", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}
