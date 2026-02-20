package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Normalized error response DTO.
 */
@Data
@NoArgsConstructor
@Schema(description = "Normalized error response")
public class ErrorResponseDTO {

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Error detail messages")
    private List<String> messages;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp = LocalDateTime.now();

    public ErrorResponseDTO(String error, List<String> messages) {
        this.timestamp = LocalDateTime.now();
        this.error = error;
        this.messages = messages;
    }

    public ErrorResponseDTO(String error, String message) {
        this.timestamp = LocalDateTime.now();
        this.error = error;
        this.messages = Collections.singletonList(message);
    }
}
