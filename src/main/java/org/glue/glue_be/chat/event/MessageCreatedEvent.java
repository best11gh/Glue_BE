package org.glue.glue_be.chat.event;

import java.time.LocalDateTime;

public record MessageCreatedEvent(
        Long messageId,
        Long chatRoomId,
        Long senderId,
        String content,
        LocalDateTime createdAt,
        String type
) {
}