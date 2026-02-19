package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Meta endpoint for introspection.
 * Uses Reflection to dynamically expose filterable/sortable fields.
 */
@RestController
@RequestMapping("/api/meta")
@Tag(name = "Meta", description = "Metadata and introspection endpoints")
public class MetaController {

    @GetMapping("/annonces")
    @Operation(summary = "Get filterable/sortable fields for Annonce (via Reflection)")
    public ResponseEntity<Map<String, Object>> getAnnonceMetadata() {
        List<Map<String, String>> fields = Arrays.stream(Annonce.class.getDeclaredFields())
                .map(field -> Map.of(
                        "name", field.getName(),
                        "type", field.getType().getSimpleName()
                ))
                .collect(Collectors.toList());

        List<String> filterableFields = Arrays.stream(Annonce.class.getDeclaredFields())
                .map(Field::getName)
                .filter(name -> !name.equals("version"))
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "entity", "Annonce",
                "fields", fields,
                "filterableFields", filterableFields,
                "sortableFields", Arrays.stream(Annonce.class.getDeclaredFields())
                        .map(Field::getName)
                        .collect(Collectors.toList())
        ));
    }
}
