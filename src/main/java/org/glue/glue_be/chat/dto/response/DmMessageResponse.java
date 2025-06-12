package org.glue.glue_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.dto.UserSummary;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageResponse {
    private Long dmMessageId;
    private Long dmChatRoomId;
    private UserSummary sender;
    private String content;
    private LocalDateTime createdAt;
}