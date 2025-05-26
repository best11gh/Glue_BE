package org.glue.glue_be.main.dto.response;

import java.util.List;

public record CarouselVersionResponse(
        List<String> versions,
        Integer totalCount
) {

    public boolean isEmpty() {
        return versions == null || versions.isEmpty();
    }

}