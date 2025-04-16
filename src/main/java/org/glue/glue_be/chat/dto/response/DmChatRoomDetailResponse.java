package org.glue.glue_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmChatRoomDetailResponse {
    private Long dmChatRoomId;
    private Long meetingId;
    private List<ChatUserResponse> participants;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChatUserResponse {
        private Long userId;
        private String userName;
    }
}