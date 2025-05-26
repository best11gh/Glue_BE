package org.glue.glue_be.main.dto.response;

import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;

public record CarouselPresignedUrlResponse(
        String presignedUrl,
        String publicUrl,
        String fileName,
        Long carouselId
) {
    public static CarouselPresignedUrlResponse fromEntity(GetPresignedUrlResponse response, String fileName, Long carouselId) {
        return new CarouselPresignedUrlResponse(
                response.getPresignedUrl(),
                response.getPublicUrl(),
                fileName,
                carouselId
        );
    }
}
