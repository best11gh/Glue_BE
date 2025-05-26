package org.glue.glue_be.main.dto.response;

import org.glue.glue_be.aws.dto.GetPresignedUrlResponse;

// Presigned URL 응답 DTO (기존 GetPresignedUrlResponse 활용)
public record CarouselPresignedUrlResponse(
        String presignedUrl,
        String publicUrl,
        String fileName,
        Long carouselId
) {
    // 기존 GetPresignedUrlResponse에서 변환하는 정적 메서드
    public static CarouselPresignedUrlResponse fromEntity(GetPresignedUrlResponse response, String fileName, Long carouselId) {
        return new CarouselPresignedUrlResponse(
                response.getPresignedUrl(),
                response.getPublicUrl(),
                fileName,
                carouselId
        );
    }
}
