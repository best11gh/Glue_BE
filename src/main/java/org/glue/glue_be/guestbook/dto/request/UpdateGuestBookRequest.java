package org.glue.glue_be.guestbook.dto.request;

import jakarta.validation.constraints.*;

public record UpdateGuestBookRequest(

        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 200, message = "내용은 최대 200자까지 가능합니다.")
        String content,

        @NotNull(message = "비공개 여부는 필수입니다.")
        Boolean secret
) {
}
