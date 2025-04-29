package org.glue.glue_be.chat.dto.response;

import lombok.Getter;

@Getter
public class DmChatRoomCreateResult {
    private DmChatRoomDetailResponse detail;
    private DmActionResponse status;

    public DmChatRoomCreateResult(DmChatRoomDetailResponse detail, DmActionResponse status) {
        this.detail = detail;
        this.status = status;
    }
}
