package org.glue.glue_be.notice.dto.response;

import lombok.Builder;
import org.glue.glue_be.notice.entity.NoticeImage;

@Builder
public record NoticeImageResponse(
        Long noticeImageId,
        String imageUrl,
        Integer imageOrder
) {
    public static NoticeImageResponse fromEntity(NoticeImage noticeImage) {
        return NoticeImageResponse.builder()
                .noticeImageId(noticeImage.getId())
                .imageUrl(noticeImage.getImageUrl())
                .imageOrder(noticeImage.getImageOrder())
                .build();
    }
}
