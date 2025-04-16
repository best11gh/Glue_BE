package org.glue.glue_be.chat.mapper;

import org.glue.glue_be.chat.dto.response.DmChatRoomDetailResponse;
import org.glue.glue_be.chat.dto.response.DmChatRoomListResponse;
import org.glue.glue_be.chat.dto.response.DmMessageResponse;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 채팅 관련 DTO <-> Entity 매핑을 담당하는 매퍼 클래스
 */
@Component
public class DmResponseMapper {

    /**
     * User 엔티티를 ChatUserResponse DTO로 변환
     */
    public DmChatRoomDetailResponse.ChatUserResponse toChatUserResponse(User user) {
        return DmChatRoomDetailResponse.ChatUserResponse.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .build();
    }

    /**
     * DmChatRoom 엔티티와 참여자 목록을 DmChatRoomDetailResponse DTO로 변환
     */
    public DmChatRoomDetailResponse toChatRoomDetailResponse(DmChatRoom dmChatRoom, List<DmUserChatroom> participants) {
        List<DmChatRoomDetailResponse.ChatUserResponse> participantResponses = participants.stream()
                .map(dmUserChatroom -> toChatUserResponse(dmUserChatroom.getUser()))
                .collect(Collectors.toList());

        return DmChatRoomDetailResponse.builder()
                .dmChatRoomId(dmChatRoom.getId())
                .meetingId(dmChatRoom.getMeeting().getMeetingId())
                .participants(participantResponses)
                .createdAt(dmChatRoom.getCreatedAt())
                .updatedAt(dmChatRoom.getUpdatedAt())
                .build();
    }

    /**
     * 채팅방 목록 응답 DTO 변환
     */
    public DmChatRoomListResponse toChatRoomListResponse(
            DmChatRoom chatRoom, User otherUser, DmMessage lastMessage, boolean hasUnreadMessages) {
        return DmChatRoomListResponse.builder()
                .dmChatRoomId(chatRoom.getId())
                .meetingId(chatRoom.getMeeting().getMeetingId())
                .otherUserId(otherUser.getUserId())
                .lastMessage(lastMessage != null ? lastMessage.getDmMessageContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .hasUnreadMessages(hasUnreadMessages)
                .build();
    }

    /**
     * DmMessage 엔티티를 DmMessageResponse DTO로 변환
     */
    public DmMessageResponse toMessageResponse(DmMessage dmMessage) {
        User sender = dmMessage.getUser();

        return DmMessageResponse.builder()
                .dmMessageId(dmMessage.getId())
                .dmChatRoomId(dmMessage.getDmChatRoom().getId())
                .senderId(sender.getUserId())
                .senderName(sender.getUserName())
                .content(dmMessage.getDmMessageContent())
                .createdAt(dmMessage.getCreatedAt())
                .isRead(dmMessage.getIsRead())
                .build();
    }
}