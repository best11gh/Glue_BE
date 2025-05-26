package org.glue.glue_be.main.dto.response;

public record CarouselBulkDeleteResponse(
        String version,
        Integer totalCount,
        Integer successCount,
        Integer failedCount
) { }