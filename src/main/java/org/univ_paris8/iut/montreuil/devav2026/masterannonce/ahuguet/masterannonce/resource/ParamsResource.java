package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.resource;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;

/**
 * Demonstration endpoint for QueryParams and PathParams.
 * GET /api/params/{id}?name=xxx&page=1
 */
@Path("/params")
@Tag(name = "Test", description = "Test endpoints")
public class ParamsResource {

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Test QueryParams and PathParams")
    public Response getWithParams(
            @Parameter(description = "Resource ID") @PathParam("id") Long id,
            @Parameter(description = "Name query parameter") @QueryParam("name") String name,
            @Parameter(description = "Page number") @QueryParam("page") @DefaultValue("1") int page) {

        Map<String, Object> result = new HashMap<>();
        result.put("pathParam_id", id);
        result.put("queryParam_name", name);
        result.put("queryParam_page", page);
        return Response.ok(result).build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Test QueryParams only")
    public Response getWithQueryParamsOnly(
            @Parameter(description = "Search keyword") @QueryParam("keyword") String keyword,
            @Parameter(description = "Page number") @QueryParam("page") @DefaultValue("1") int page) {

        Map<String, Object> result = new HashMap<>();
        result.put("queryParam_keyword", keyword);
        result.put("queryParam_page", page);
        return Response.ok(result).build();
    }
}
