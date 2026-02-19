package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto.ErrorResponseDTO;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Centralized mapper for all BusinessException subclasses.
 * Returns normalized JSON: { "error": "TYPE", "messages": ["details"] }
 */
@Provider
public class BusinessExceptionMapper implements ExceptionMapper<BusinessException> {

    private static final Logger LOG = LoggerFactory.getLogger(BusinessExceptionMapper.class);

    @Override
    public Response toResponse(BusinessException ex) {
        LOG.warn("Business exception [{}]: {}", ex.getErrorType(), ex.getMessage());

        ErrorResponseDTO error = new ErrorResponseDTO(ex.getErrorType(), ex.getMessages());
        return Response.status(ex.getHttpStatus())
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
