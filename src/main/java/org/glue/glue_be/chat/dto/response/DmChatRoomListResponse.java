package org.glue.glue_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
public class DmChatRoomListResponse {
    private Long dmChatRoomId;
    private Long meetingId;
    private Long otherUserId;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean hasUnreadMessages;
}