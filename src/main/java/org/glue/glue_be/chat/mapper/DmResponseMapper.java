package org.glue.glue_be.chat.mapper;

import org.glue.glue_be.chat.dto.response.DmChatRoomDetailResponse;
import org.glue.glue_be.chat.dto.response.DmChatRoomListResponse;
import org.glue.glue_be.chat.dto.response.DmMessageResponse;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.user.entity.ProfileImage;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.ProfileImageRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 채팅 관련 DTO <-> Entity 매핑을 담당하는 매퍼 클래스
 */
@Component
public class DmResponseMapper {
    private final ProfileImageRepository profileImageRepository;

    public DmResponseMapper(ProfileImageRepository profileImageRepository) {
        this.profileImageRepository = profileImageRepository;
    }

    // User 엔티티를 ChatUserResponse DTO로 변환
    public UserSummary toChatUserResponse(User user) {
        ProfileImage profileImage = profileImageRepository.findByUser_UserId(user.getUserId());

        return UserSummary.builder()
                .userId(user.getUserId())
                .userName(user.getUserName())
                .profileImageUrl(profileImage != null ? profileImage.getProfileImageUrl() : null)
                .build();
    }

    //DmChatRoom 엔티티와 참여자 목록을 DmChatRoomDetailResponse DTO로 변환
    public DmChatRoomDetailResponse toChatRoomDetailResponse(DmChatRoom dmChatRoom, List<DmUserChatroom> participants, UUID userUuId, Integer invitationStatus) {
        List<UserSummary> participantResponses = participants.stream()
                .map(dmUserChatroom -> toChatUserResponse(dmUserChatroom.getUser()))
                .collect(Collectors.toList());

        DmChatRoomDetailResponse.DmChatRoomDetailResponseBuilder builder = DmChatRoomDetailResponse.builder()
                .dmChatRoomId(dmChatRoom.getId())
                .meetingId(dmChatRoom.getMeeting().getMeetingId())
                .participants(participantResponses)
                .createdAt(dmChatRoom.getCreatedAt())
                .updatedAt(dmChatRoom.getUpdatedAt());

        if (userUuId != null) {
            Integer isPushNotificationOn = dmChatRoom.getDmUserChatrooms().stream()
                    .filter(duc -> duc.getUser().getUuid().equals(userUuId))
                    .findFirst()
                    .map(DmUserChatroom::getPushNotificationOn)
                    .orElse(1); //default: 1

            builder.isPushNotificationOn(isPushNotificationOn);
        }

        if (invitationStatus != -1) {
            builder.invitationStatus(invitationStatus);
        }

        return builder.build();
    }

    // 채팅방 목록 응답 DTO 변환
    public DmChatRoomListResponse toChatRoomListResponse(
            DmChatRoom chatRoom, User otherUser, DmMessage lastMessage, boolean hasUnreadMessages) {
        return DmChatRoomListResponse.builder()
                .dmChatRoomId(chatRoom.getId())
                .meetingId(chatRoom.getMeeting().getMeetingId())
                .otherUser(UserSummary.builder()
                        .userId(otherUser.getUserId())
                        .userName(otherUser.getUserName())
                        .profileImageUrl(otherUser.getProfileImage() != null
                                ? otherUser.getProfileImage().getProfileImageUrl()
                                : null)
                        .build())
                .lastMessage(lastMessage != null ? lastMessage.getDmMessageContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .hasUnreadMessages(hasUnreadMessages)
                .build();
    }

    // DmMessage 엔티티를 DmMessageResponse DTO로 변환
    public DmMessageResponse toMessageResponse(DmMessage dmMessage) {
        User sender = dmMessage.getUser();

        return DmMessageResponse.builder()
                .dmMessageId(dmMessage.getId())
                .dmChatRoomId(dmMessage.getDmChatRoom().getId())
                .sender(UserSummary.builder()
                        .userId(sender.getUserId())
                        .userName(sender.getUserName())
                        .profileImageUrl(sender.getProfileImage() != null
                                ? sender.getProfileImage().getProfileImageUrl()
                                : null)
                        .build())
                .content(dmMessage.getDmMessageContent())
                .createdAt(dmMessage.getCreatedAt())
                .isRead(dmMessage.getIsRead())
                .build();
    }
}