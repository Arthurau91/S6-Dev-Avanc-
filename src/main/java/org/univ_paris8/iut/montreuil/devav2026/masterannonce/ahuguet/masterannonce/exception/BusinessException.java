package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception;

import java.util.Collections;
import java.util.List;

/**
 * Base business exception with error type and messages.
 */
public class BusinessException extends RuntimeException {

    private final String errorType;
    private final List<String> messages;
    private final int httpStatus;

    public BusinessException(String errorType, String message, int httpStatus) {
        super(message);
        this.errorType = errorType;
        this.messages = Collections.singletonList(message);
        this.httpStatus = httpStatus;
    }

    public BusinessException(String errorType, List<String> messages, int httpStatus) {
        super(String.join(", ", messages));
        this.errorType = errorType;
        this.messages = messages;
        this.httpStatus = httpStatus;
    }

    public String getErrorType() { return errorType; }
    public List<String> getMessages() { return messages; }
    public int getHttpStatus() { return httpStatus; }
}
