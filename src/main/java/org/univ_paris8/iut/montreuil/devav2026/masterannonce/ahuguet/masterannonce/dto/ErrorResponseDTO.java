package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Normalized error response DTO.
 */
@Schema(description = "Normalized error response")
public class ErrorResponseDTO {

    @Schema(description = "Error type", example = "VALIDATION_ERROR")
    private String error;

    @Schema(description = "Error detail messages")
    private List<String> messages;

    @Schema(description = "Timestamp of the error")
    private LocalDateTime timestamp;

    public ErrorResponseDTO() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponseDTO(String error, List<String> messages) {
        this();
        this.error = error;
        this.messages = messages;
    }

    public ErrorResponseDTO(String error, String message) {
        this();
        this.error = error;
        this.messages = Collections.singletonList(message);
    }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getMessages() { return messages; }
    public void setMessages(List<String> messages) { this.messages = messages; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
