package org.glue.glue_be.chat.dto.response;

import org.glue.glue_be.common.dto.MeetingSummary;
import org.glue.glue_be.common.dto.UserSummary;

import java.time.LocalDateTime;

public record GroupChatRoomListResponse(
        Long groupChatroomId,
        MeetingSummary meeting,
        String lastMessage,
        LocalDateTime lastMessageTime,
        boolean hasUnreadMessages
) {
    // Static builder method to maintain compatibility with builder pattern
    public static Builder builder() {
        return new Builder();
    }

    // Builder class to support the builder pattern
    public static class Builder {
        private Long groupChatroomId;
        private MeetingSummary meeting;
        private String lastMessage;
        private LocalDateTime lastMessageTime;
        private boolean hasUnreadMessages;

        public Builder groupChatroomId(Long groupChatroomId) {
            this.groupChatroomId = groupChatroomId;
            return this;
        }

        public Builder meeting(MeetingSummary meeting) {
            this.meeting = meeting;
            return this;
        }

        public Builder lastMessage(String lastMessage) {
            this.lastMessage = lastMessage;
            return this;
        }

        public Builder lastMessageTime(LocalDateTime lastMessageTime) {
            this.lastMessageTime = lastMessageTime;
            return this;
        }

        public Builder hasUnreadMessages(boolean hasUnreadMessages) {
            this.hasUnreadMessages = hasUnreadMessages;
            return this;
        }

        public GroupChatRoomListResponse build() {
            return new GroupChatRoomListResponse(
                    groupChatroomId,
                    meeting,
                    lastMessage,
                    lastMessageTime,
                    hasUnreadMessages
            );
        }
    }
}