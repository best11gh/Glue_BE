package org.glue.glue_be.main.dto.response;

import org.glue.glue_be.main.dto.CarouselImageDto;

import java.util.List;

public record MainCarouselResponse(
        String version,
        List<CarouselImageDto> images,
        Integer totalCount
) {}

