package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import org.mapstruct.*;
import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

/**
 * MapStruct mapper for Annonce entity <-> DTO conversions.
 * componentModel = "spring" is set globally via compiler arg.
 *
 * Rules enforced:
 * - No manual "new DTO()" in business logic
 * - No setter-by-setter mapping
 * - @MappingTarget for updates
 */
@Mapper(componentModel = "spring")
public interface AnnonceMapper {

    /**
     * Entity -> Response DTO.
     * Maps nested author/category fields to flat DTO fields.
     */
    @Mapping(source = "status", target = "status", qualifiedByName = "statusToString")
    @Mapping(source = "author.username", target = "authorUsername")
    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "category.label", target = "categoryLabel")
    @Mapping(source = "category.id", target = "categoryId")
    AnnonceDTO toDTO(Annonce entity);

    /**
     * Create DTO -> Entity.
     * Ignores relationships (author, category) — set in service layer.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    Annonce toEntity(AnnonceCreateDTO dto);

    /**
     * Full update DTO -> existing Entity (PUT) using @MappingTarget.
     * Ignores id, date, status, author — preserves them.
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntityFromDTO(AnnonceUpdateDTO dto, @MappingTarget Annonce entity);

    /**
     * Partial update DTO -> existing Entity (PATCH) using @MappingTarget.
     * Uses NullValuePropertyMappingStrategy.IGNORE to skip null fields.
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    void patchEntityFromDTO(AnnoncePatchDTO dto, @MappingTarget Annonce entity);

    /**
     * Convert AnnonceStatus enum to String.
     */
    @Named("statusToString")
    default String statusToString(org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.AnnonceStatus status) {
        return status != null ? status.name() : null;
    }
}
