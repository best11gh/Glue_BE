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
public class DmChatRoomListResponse {
    private Long dmChatRoomId;
    private Long meetingId;
    private UserSummary otherUser;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private boolean hasUnreadMessages;
}