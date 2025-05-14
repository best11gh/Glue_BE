package org.glue.glue_be.guestbook.dto.request;

import jakarta.validation.constraints.*;

public record UpdateGuestBookRequest(

        @Size(max = 200, message = "내용은 최대 200자까지 가능합니다.")
        String content,

        Boolean secret
) {
}
