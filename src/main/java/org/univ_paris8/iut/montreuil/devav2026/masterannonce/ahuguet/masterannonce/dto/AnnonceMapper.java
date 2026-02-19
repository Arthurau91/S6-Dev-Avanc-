package org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.dto;

import org.univ_paris8.iut.montreuil.devav2026.masterannonce.ahuguet.masterannonce.model.Annonce;

/**
 * Mapper using the Builder pattern for Entity <-> DTO conversions.
 */
public final class AnnonceMapper {

    private AnnonceMapper() {}

    /**
     * Convert Annonce entity to AnnonceDTO using Builder pattern.
     */
    public static AnnonceDTO toDTO(Annonce entity) {
        if (entity == null) return null;

        AnnonceDTO.Builder builder = AnnonceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .adress(entity.getAdress())
                .mail(entity.getMail())
                .date(entity.getDate())
                .status(entity.getStatus() != null ? entity.getStatus().name() : null)
                .version(entity.getVersion());

        if (entity.getAuthor() != null) {
            builder.authorUsername(entity.getAuthor().getUsername())
                   .authorId(entity.getAuthor().getId());
        }

        if (entity.getCategory() != null) {
            builder.categoryLabel(entity.getCategory().getLabel())
                   .categoryId(entity.getCategory().getId());
        }

        return builder.build();
    }

    /**
     * Convert AnnonceCreateDTO to Annonce entity using Builder-style setters.
     */
    public static Annonce toEntity(AnnonceCreateDTO dto) {
        if (dto == null) return null;

        Annonce annonce = new Annonce();
        annonce.setTitle(dto.getTitle());
        annonce.setDescription(dto.getDescription());
        annonce.setAdress(dto.getAdress());
        annonce.setMail(dto.getMail());
        return annonce;
    }

    /**
     * Apply AnnonceUpdateDTO fields to an existing Annonce entity.
     */
    public static void applyUpdate(AnnonceUpdateDTO dto, Annonce entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setAdress(dto.getAdress());
        entity.setMail(dto.getMail());
        entity.setVersion(dto.getVersion());
    }

    /**
     * Apply non-null fields from AnnoncePatchDTO to an existing Annonce entity.
     * Only fields that are not null in the DTO are applied (partial update).
     */
    public static void applyPatch(AnnoncePatchDTO dto, Annonce entity) {
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getAdress() != null) entity.setAdress(dto.getAdress());
        if (dto.getMail() != null) entity.setMail(dto.getMail());
        entity.setVersion(dto.getVersion());
    }
}
