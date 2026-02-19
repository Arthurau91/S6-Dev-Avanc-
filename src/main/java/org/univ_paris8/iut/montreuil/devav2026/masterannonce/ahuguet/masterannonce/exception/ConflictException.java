package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception;

/**
 * Thrown on business logic conflict (HTTP 409).
 * Example: trying to modify a PUBLISHED announcement, optimistic lock failure.
 */
public class ConflictException extends BusinessException {

    public ConflictException(String message) {
        super("CONFLICT", message, 409);
    }
}
