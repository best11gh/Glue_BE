package org.glue.glue_be.main.dto;

import org.glue.glue_be.main.entity.MainCarousel;

public record CarouselImageDto(
        Long id,
        String imageUrl,
        Integer displayOrder,
        String description
) {
    public static CarouselImageDto fromEntity(MainCarousel carousel) {
        return new CarouselImageDto(
                carousel.getId(),
                carousel.getImageUrl(),
                carousel.getDisplayOrder(),
                carousel.getDescription()
        );
    }
}
