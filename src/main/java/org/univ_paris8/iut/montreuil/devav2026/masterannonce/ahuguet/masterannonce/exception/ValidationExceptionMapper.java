package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.ErrorResponseDTO;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps Bean Validation ConstraintViolationException to normalized JSON error response (HTTP 400).
 */
@Provider
public class ValidationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionMapper.class);

    @Override
    public Response toResponse(ConstraintViolationException ex) {
        List<String> messages = ex.getConstraintViolations().stream()
                .map(this::formatViolation)
                .collect(Collectors.toList());

        LOG.warn("Validation error: {}", messages);

        ErrorResponseDTO error = new ErrorResponseDTO("VALIDATION_ERROR", messages);
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String formatViolation(ConstraintViolation<?> violation) {
        String field = "";
        String[] pathParts = violation.getPropertyPath().toString().split("\\.");
        if (pathParts.length > 0) {
            field = pathParts[pathParts.length - 1];
        }
        return field + " : " + violation.getMessage();
    }
}
