package org.glue.glue_be.chat.event;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.response.DmChatRoomListResponse;
import org.glue.glue_be.chat.dto.response.GroupChatRoomListResponse;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
import org.glue.glue_be.chat.repository.dm.DmMessageRepository;
import org.glue.glue_be.chat.repository.dm.DmUserChatroomRepository;
import org.glue.glue_be.chat.repository.group.GroupChatRoomRepository;
import org.glue.glue_be.chat.repository.group.GroupMessageRepository;
import org.glue.glue_be.chat.repository.group.GroupUserChatRoomRepository;
import org.glue.glue_be.common.dto.MeetingSummary;
import org.glue.glue_be.user.entity.User;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import org.glue.glue_be.common.dto.UserSummary;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChatRoomListUpdateHandler {
    @PersistenceContext
    private EntityManager entityManager;

    private final SimpMessagingTemplate messagingTemplate;

    // DM 관련
    private final DmUserChatroomRepository dmUserChatroomRepository;
    private final DmMessageRepository dmMessageRepository;

    // 그룹 관련
    private final GroupUserChatRoomRepository groupUserChatroomRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupChatRoomRepository groupChatRoomRepository;

    @EventListener
    @Transactional
    public void handleNewMessage(MessageCreatedEvent event) {
        updateChatRoomList(event.chatRoomId(), event.senderId(), event.type());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleMessageRead(MessageReadEvent event) {
        entityManager.clear(); // JPA 1차 캐싱 삭제
        updateChatRoomList(event.chatRoomId(), event.userId(), event.chatRoomType());
    }

    private void updateChatRoomList(Long chatRoomId, Long targetUserId, String chatRoomType) {
        List<? extends Object> participants = getChatRoomParticipants(chatRoomId, chatRoomType);

        // DM과 GROUP 모두 개별 전송으로 통일
        participants.forEach(participant -> {
            User receiver = extractUser(participant, chatRoomType);

            // 각 사용자별로 개인화된 DTO 생성
            ChatRoomListUpdateDto updateDto = createChatRoomUpdateDto(chatRoomId, receiver, chatRoomType);

            messagingTemplate.convertAndSendToUser(
                    receiver.getUserId().toString(),
                    "/queue/chatroom-list-update",
                    updateDto
            );

            log.debug("{} 채팅방 목록 업데이트 알림 전송: receiverId={}, chatRoomId={}, targetUserId={}",
                    chatRoomType, receiver.getUserId(), chatRoomId, targetUserId);
        });
    }

    private ChatRoomListUpdateDto createChatRoomUpdateDto(Long chatRoomId, User currentUser, String chatRoomType) {
        return switch (chatRoomType) {
            case "DM" -> {
                DmChatRoomListResponse dmResponse = createDmChatRoomResponse(chatRoomId, currentUser);
                yield ChatRoomListUpdateDto.fromDm(dmResponse, LocalDateTime.now());
            }
            case "GROUP" -> {
                GroupChatRoomListResponse groupResponse = createGroupChatRoomResponse(chatRoomId, currentUser);
                yield ChatRoomListUpdateDto.fromGroup(groupResponse, LocalDateTime.now());
            }
            default -> throw new IllegalArgumentException("알 수 없는 채팅 타입: " + chatRoomType);
        };
    }

    private List<? extends Object> getChatRoomParticipants(Long chatRoomId, String chatRoomType) {
        return switch (chatRoomType) {
            case "DM" -> dmUserChatroomRepository.findByDmChatRoom_Id(chatRoomId);
            case "GROUP" -> groupUserChatroomRepository.findByGroupChatroom_GroupChatroomId(chatRoomId);
            default -> throw new IllegalArgumentException("알 수 없는 채팅 타입: " + chatRoomType);
        };
    }

    private User extractUser(Object participant, String chatRoomType) {
        return switch (chatRoomType) {
            case "DM" -> ((DmUserChatroom) participant).getUser();
            case "GROUP" -> ((GroupUserChatRoom) participant).getUser();
            default -> throw new IllegalArgumentException("알 수 없는 채팅 타입: " + chatRoomType);
        };
    }

    // DM 채팅방 정보 생성
    private DmChatRoomListResponse createDmChatRoomResponse(Long chatRoomId, User currentUser) {
        List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom_Id(chatRoomId);

        User otherUser = participants.stream()
                .map(DmUserChatroom::getUser)
                .filter(user -> !user.getUserId().equals(currentUser.getUserId()))
                .findFirst()
                .orElse(currentUser);

        DmMessage lastMessage = dmMessageRepository.findTopByDmChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .orElse(null);

        // 안읽은 메시지 여부 확인
        Long currentUserLastReadMessageId = getCurrentUserLastReadMessageId(currentUser.getUserId(), chatRoomId);
        long latestMessageId = getLatestMessageId(chatRoomId);
        boolean hasUnreadMessages = currentUserLastReadMessageId < latestMessageId;

        // UserSummary 생성
        UserSummary otherUserSummary = new UserSummary(
                otherUser.getUserId(),
                otherUser.getNickname(),
                otherUser.getProfileImageUrl()
        );

        return DmChatRoomListResponse.builder()
                .dmChatRoomId(chatRoomId)
                .meetingId(null) // DM은 모임 ID 없음
                .otherUser(otherUserSummary)
                .lastMessage(lastMessage != null ? lastMessage.getDmMessageContent() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .hasUnreadMessages(hasUnreadMessages)
                .build();
    }

    // 그룹 채팅방 정보 생성
    private GroupChatRoomListResponse createGroupChatRoomResponse(Long chatRoomId, User currentUser) {
        // 그룹 채팅방 조회
        GroupChatRoom groupChatRoom = groupChatRoomRepository.findById(chatRoomId)
                .orElseThrow(() -> new IllegalArgumentException("그룹 채팅방을 찾을 수 없습니다: " + chatRoomId));

        // 마지막 메시지 조회
        GroupMessage lastMessage = groupMessageRepository.findTopByGroupChatroomOrderByCreatedAtDesc(groupChatRoom)
                .orElse(null);

        // 현재 사용자의 마지막 읽은 메시지 ID 조회
        Long currentUserLastReadMessageId = getCurrentUserLastReadMessageIdForGroup(currentUser.getUserId(), chatRoomId);

        // 채팅방의 가장 최신 메시지 ID 조회
        long latestMessageId = getLatestMessageIdForGroup(groupChatRoom);

        // 안읽은 메시지 여부 확인
        boolean hasUnreadMessages = currentUserLastReadMessageId < latestMessageId;

        System.out.println("현재 사용자의 마지막 읽은 메시지 ID: " + currentUserLastReadMessageId);
        System.out.println("채팅방의 가장 최신 메시지 ID: " + latestMessageId);

        // 참여자 수 조회
        int memberCount = groupUserChatroomRepository.findByGroupChatroom_GroupChatroomId(chatRoomId).size();

        // MeetingSummary 생성
        MeetingSummary meetingSummary = new MeetingSummary(
                groupChatRoom.getMeeting().getMeetingId(),
                groupChatRoom.getMeeting().getMeetingTitle(),
                groupChatRoom.getMeeting().getMeetingImageUrl(),
                memberCount
        );

        return GroupChatRoomListResponse.builder()
                .groupChatroomId(chatRoomId)
                .meeting(meetingSummary)
                .lastMessage(lastMessage != null ? lastMessage.getMessage() : null)
                .lastMessageTime(lastMessage != null ? lastMessage.getCreatedAt() : null)
                .hasUnreadMessages(hasUnreadMessages)
                .build();
    }

    // DM 관련 헬퍼 메서드들
    private Long getCurrentUserLastReadMessageId(Long userId, Long chatRoomId) {
        return dmUserChatroomRepository
                .findByUser_UserIdAndDmChatRoom_Id(userId, chatRoomId)
                .map(DmUserChatroom::getLastReadMessageId)
                .orElse(0L);
    }

    private long getLatestMessageId(Long chatRoomId) {
        return dmMessageRepository.findTopByDmChatRoomIdOrderByCreatedAtDesc(chatRoomId)
                .map(DmMessage::getId)
                .orElse(0L);
    }

    // 그룹 관련 헬퍼 메서드들
    private Long getCurrentUserLastReadMessageIdForGroup(Long userId, Long chatRoomId) {
        return groupUserChatroomRepository.findByUser_UserIdAndGroupChatroom_GroupChatroomId(userId, chatRoomId)
                .map(GroupUserChatRoom::getLastReadMessageId)
                .orElse(0L);
    }

    private long getLatestMessageIdForGroup(GroupChatRoom groupChatRoom) {
        return groupMessageRepository.findTopByGroupChatroomOrderByCreatedAtDesc(groupChatRoom)
                .map(GroupMessage::getGroupMessageId)
                .orElse(0L);
    }
}