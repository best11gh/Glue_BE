package org.glue.glue_be.chat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DmReadStatusUpdateResponse {
    private Long dmChatRoomId;
    private Long readerId;
    private List<DmMessageResponse> readMessages;
}
