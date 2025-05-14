package org.glue.glue_be.chat.dto.response;

import lombok.Getter;

@Getter
public class DmChatRoomCreateResult {
    private DmChatRoomDetailResponse detail;
    private ActionResponse status;

    public DmChatRoomCreateResult(DmChatRoomDetailResponse detail, ActionResponse status) {
        this.detail = detail;
        this.status = status;
    }
}
