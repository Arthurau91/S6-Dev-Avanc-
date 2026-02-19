package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception;

/**
 * Thrown when user lacks permission (HTTP 403).
 */
public class ForbiddenException extends BusinessException {

    public ForbiddenException(String message) {
        super("FORBIDDEN", message, 403);
    }
}
