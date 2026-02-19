package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

/**
 * Test endpoint: GET /api/helloWorld
 */
@Path("/helloWorld")
@Tag(name = "Test", description = "Test endpoints")
public class HelloWorldResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Hello World test endpoint")
    public Response helloWorld() {
        return Response.ok(Collections.singletonMap("message", "Hello World!")).build();
    }
}
