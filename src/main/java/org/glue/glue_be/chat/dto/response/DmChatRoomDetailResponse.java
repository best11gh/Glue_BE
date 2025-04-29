package org.glue.glue_be.chat.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.glue.glue_be.common.dto.UserSummary;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmChatRoomDetailResponse {
    private Long dmChatRoomId;
    private Long meetingId;
    private List<UserSummary> participants;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer isPushNotificationOn;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}