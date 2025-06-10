package org.glue.glue_be.chat.event;

import org.glue.glue_be.chat.dto.response.DmChatRoomListResponse;
import org.glue.glue_be.chat.dto.response.GroupChatRoomListResponse;
import org.glue.glue_be.common.dto.MeetingSummary;
import org.glue.glue_be.common.dto.UserSummary;

import java.time.LocalDateTime;

public record ChatRoomListUpdateDto(
        Long chatRoomId,
        String chatRoomType,           // "DM" 또는 "MEETING"
        UserSummary otherUser,         // DM 상대방 정보 (모임톡인 경우 null)
        MeetingSummary meeting,        // 모임톡 정보 (DM인 경우 null)
        String lastMessage,
        LocalDateTime lastMessageTime,
        boolean hasUnreadMessages
) {
    // DM 채팅방용 정적 팩토리 메서드
    public static ChatRoomListUpdateDto fromDm(DmChatRoomListResponse response, LocalDateTime messageTime) {
        return new ChatRoomListUpdateDto(
                response.getDmChatRoomId(),
                "DM",
                response.getOtherUser(),
                null, // DM은 모임 정보 필요 없음
                response.getLastMessage(),
                messageTime,
                response.isHasUnreadMessages()
        );
    }

    // 모임톡용 정적 팩토리 메서드
    public static ChatRoomListUpdateDto fromGroup(GroupChatRoomListResponse response, LocalDateTime messageTime) {
        return new ChatRoomListUpdateDto(
                response.groupChatroomId(),
                "GROUP",
                null, // 모임톡은 상대방 정보 필요 없음
                response.meeting(),
                response.lastMessage(),
                messageTime,
                response.hasUnreadMessages()
        );
    }
}