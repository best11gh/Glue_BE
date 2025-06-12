package org.glue.glue_be.chat.event;

public record MessageReadEvent(
        Long userId,           // 읽음 처리한 사용자 ID
        Long chatRoomId,       // 채팅방 ID
        String chatRoomType    // "DM" 또는 "GROUP"
) {
}