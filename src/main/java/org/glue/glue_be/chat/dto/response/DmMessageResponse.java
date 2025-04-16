package org.glue.glue_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmMessageResponse {
    private Long dmMessageId;
    private Long dmChatRoomId;
    private Long senderId;
    private String senderName;
    private String content;
    private Integer isRead;
    private LocalDateTime createdAt;
}