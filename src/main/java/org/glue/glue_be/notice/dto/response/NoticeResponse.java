package org.glue.glue_be.notice.dto.response;

import java.util.List;
import lombok.Builder;

@Builder
public record NoticeResponse(
        Long noticeId,
        String title,
        String content,
        List<NoticeImageResponse> imageUrl
) {
}
