package org.glue.glue_be.notice.dto.request;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record NoticeRequest(
        @NotBlank(message = "공지 제목은 필수값입니다.")
        String title,

        @NotBlank(message = "공지 내용은 필수값입니다.")
        String content,

        List<String> imageUrls
) {
}
