package org.glue.glue_be.chat.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DmChatRoomCreateRequest {
    private Long meetingId;
    private List<Long> userIds; // 초대할 사용자 ID 목록
}
