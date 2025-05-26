package org.glue.glue_be.main.dto.response;

import org.glue.glue_be.main.entity.CarouselDeployVersion;

public record CarouselDeployVersionResponse(
        Long id,
        String version,
        Boolean isActive,
        String description
) {

    public static CarouselDeployVersionResponse fromEntity(CarouselDeployVersion entity) {
        return new CarouselDeployVersionResponse(
                entity.getId(),
                entity.getVersion(),
                entity.getIsActive(),
                entity.getDescription()
        );
    }

    @Override
    public String toString() {
        return String.format(
                "CarouselDeployVersionResponse{id=%d, version='%s', isActive=%s, description='%s'}",
                id, version, isActive, description
        );
    }
}