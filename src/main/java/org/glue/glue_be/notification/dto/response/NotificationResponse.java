package org.glue.glue_be.notification.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record NotificationResponse(
        Long notificationId,
        String type,
        String title,
        String content,
        Long targetId,
        Long hostId,
        LocalDateTime createdAt
) {}
