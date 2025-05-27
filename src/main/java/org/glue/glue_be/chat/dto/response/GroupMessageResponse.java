package org.glue.glue_be.chat.dto.response;

import org.glue.glue_be.common.dto.UserSummary;

import java.time.LocalDateTime;

public record GroupMessageResponse(
        Long groupMessageId,
        Long groupChatroomId,
        UserSummary sender,
        String message,
        LocalDateTime createdAt
) {
    // Static builder method to maintain compatibility with builder pattern
    public static Builder builder() {
        return new Builder();
    }

    // Builder class to support the builder pattern
    public static class Builder {
        private Long groupMessageId;
        private Long groupChatroomId;
        private UserSummary sender;
        private String message;
        private LocalDateTime createdAt;

        public Builder groupMessageId(Long groupMessageId) {
            this.groupMessageId = groupMessageId;
            return this;
        }

        public Builder groupChatroomId(Long groupChatroomId) {
            this.groupChatroomId = groupChatroomId;
            return this;
        }

        public Builder sender(UserSummary sender) {
            this.sender = sender;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public GroupMessageResponse build() {
            return new GroupMessageResponse(
                    groupMessageId,
                    groupChatroomId,
                    sender,
                    message,
                    createdAt
            );
        }
    }
}