package org.glue.glue_be.chat.mapper;

import org.glue.glue_be.chat.dto.response.GroupChatRoomDetailResponse;
import org.glue.glue_be.chat.dto.response.GroupChatRoomListResponse;
import org.glue.glue_be.chat.dto.response.GroupMessageResponse;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
import org.glue.glue_be.common.dto.MeetingSummary;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.user.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GroupResponseMapper {
    String meetingImageUrl = "dummy"; //TODO

    // User 엔티티를 ChatUserResponse DTO로 변환
    public UserSummary toChatUserResponse(User user) {

        return UserSummary.builder()
                .userId(user.getUserId())
                .userNickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    // 그룹 채팅방 상세 정보 매핑
    public GroupChatRoomDetailResponse toChatRoomDetailResponse(
            GroupChatRoom groupChatRoom,
            List<GroupUserChatRoom> participants,
            Long currentUserId) {

        Meeting meeting = groupChatRoom.getMeeting();

        MeetingSummary meetingSummary = new MeetingSummary(
                meeting.getMeetingId(),
                meeting.getMeetingTitle(),
                meetingImageUrl,
                meeting.getCurrentParticipants()
        );

        List<UserSummary> userSummaries = participants.stream()
                .map(participant -> {
                    User user = participant.getUser();
                    return new UserSummary(
                            user.getUserId(),
                            user.getNickname(),
                            user.getProfileImageUrl()
                    );
                })
                .collect(Collectors.toList());

        // 현재 사용자의 알림 설정 조회
        Integer pushNotificationOn = participants.stream()
                .filter(p -> p.getUser().getUserId().equals(currentUserId))
                .findFirst()
                .map(GroupUserChatRoom::getPushNotificationOn)
                .orElse(1);

        return GroupChatRoomDetailResponse.builder()
                .groupChatroomId(groupChatRoom.getGroupChatroomId())
                .meeting(meetingSummary)
                .participants(userSummaries)
                .createdAt(groupChatRoom.getCreatedAt())
                .pushNotificationOn(pushNotificationOn)
                .build();
    }

    // 그룹 채팅방 목록 아이템 매핑
    public GroupChatRoomListResponse toChatRoomListResponse(
            GroupChatRoom groupChatRoom,
            GroupMessage lastMessage,
            boolean hasUnreadMessages,
            int participantCount) {

        Meeting meeting = groupChatRoom.getMeeting();
        MeetingSummary meetingSummary = new MeetingSummary(
                meeting.getMeetingId(),
                meeting.getMeetingTitle(),
                meetingImageUrl,
                meeting.getCurrentParticipants()
        );

        String lastMessageContent = null;
        UserSummary lastMessageSender = null;
        java.time.LocalDateTime lastMessageTime = null;

        if (lastMessage != null) {
            lastMessageContent = lastMessage.getMessage();
            lastMessageTime = lastMessage.getCreatedAt();
        }

        return GroupChatRoomListResponse.builder()
                .groupChatroomId(groupChatRoom.getGroupChatroomId())
                .meeting(meetingSummary)
                .lastMessage(lastMessageContent)
                .lastMessageTime(lastMessageTime)
                .hasUnreadMessages(hasUnreadMessages)
                .build();
    }

    // 그룹 메시지 매핑
    public GroupMessageResponse toMessageResponse(GroupMessage message) {
        User sender = message.getUser();
        UserSummary senderSummary = new UserSummary(
                sender.getUserId(),
                sender.getNickname(),
                sender.getProfileImageUrl()
        );

        return GroupMessageResponse.builder()
                .groupMessageId(message.getGroupMessageId())
                .groupChatroomId(message.getGroupChatroom().getGroupChatroomId())
                .sender(senderSummary)
                .message(message.getMessage())
                .unreadCount(message.getUnreadCount())
                .createdAt(message.getCreatedAt())
                .build();
    }
}