package org.glue.glue_be.main.dto.request;

import org.glue.glue_be.main.entity.MainCarousel;

public record MainCarouselCreateRequest(
        String version,
        String fileName,
        String description
) {
    public MainCarousel toEntity(String imageUrl, Integer displayOrder) {
        return MainCarousel.builder()
                .imageUrl(imageUrl)
                .version(this.version)
                .displayOrder(displayOrder)
                .description(this.description)
                .build();
    }
}
