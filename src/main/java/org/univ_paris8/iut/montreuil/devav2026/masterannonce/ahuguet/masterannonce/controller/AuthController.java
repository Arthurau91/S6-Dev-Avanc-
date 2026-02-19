package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginRequestDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.LoginResponseDTO;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.service.AuthService;

/**
 * REST Controller for authentication.
 * POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login and token management")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate user and obtain JWT token", responses = {
            @ApiResponse(responseCode = "200", description = "Authentication successful, token returned"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
