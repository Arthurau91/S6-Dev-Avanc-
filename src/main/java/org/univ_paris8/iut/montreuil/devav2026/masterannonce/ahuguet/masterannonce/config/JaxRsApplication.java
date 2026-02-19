package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.config;

import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.BusinessExceptionMapper;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.GenericExceptionMapper;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.exception.ValidationExceptionMapper;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.security.filter.JaasAuthenticationFilter;

import javax.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 * Entry point: /api
 */
@ApplicationPath("/api")
public class JaxRsApplication extends ResourceConfig {

    public JaxRsApplication() {
        // Scan resource packages
        packages("org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource");

        // JAX-RS providers
        register(JaasAuthenticationFilter.class);
        register(BusinessExceptionMapper.class);
        register(ValidationExceptionMapper.class);
        register(GenericExceptionMapper.class);

        // Jackson JSON
        register(JacksonFeature.class);

        // OpenAPI / Swagger
        register(OpenApiResource.class);

        // Disable Jersey's default bean validation error response (we use our own mapper)
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, false);
    }
}
