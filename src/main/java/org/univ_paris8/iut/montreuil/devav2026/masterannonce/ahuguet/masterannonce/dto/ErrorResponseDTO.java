package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

/**
 * Normalized error response DTO.
 * Format: { "error": "TYPE", "messages": ["details"] }
 */
@Schema(description = "Normalized error response")
public class ErrorResponseDTO {

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Error detail messages")
    private List<String> messages;

    public ErrorResponseDTO() {}

    public ErrorResponseDTO(String error, List<String> messages) {
        this.error = error;
        this.messages = messages;
    }

    public ErrorResponseDTO(String error, String message) {
        this.error = error;
        this.messages = Collections.singletonList(message);
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }
}
