package org.glue.glue_be.chat.dto.response;

public record GroupChatRoomCreateResult(
        GroupChatRoomDetailResponse chatroom,
        ActionResponse status
) {}