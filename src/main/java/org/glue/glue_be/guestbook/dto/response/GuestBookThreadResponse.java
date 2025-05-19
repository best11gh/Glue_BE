package org.glue.glue_be.guestbook.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import org.glue.glue_be.common.dto.UserSummary;

@Getter
@Builder
public class GuestBookThreadResponse {
    private final Long id;
    private final UserSummary writer;
    private final String content;
    private final boolean secret;
    private final LocalDateTime createdAt;
    private final GuestBookThreadResponse child;
}
