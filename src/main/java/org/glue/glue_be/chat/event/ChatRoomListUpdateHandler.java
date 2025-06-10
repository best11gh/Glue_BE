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
import java.util.Optional;
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
        switch (event.type()) {
            case "DM" -> updateDmChatRoomList(event.chatRoomId(), event.senderId(), event.createdAt());
            case "GROUP" -> updateGroupChatRoomList(event.chatRoomId(), event.senderId(), event.createdAt());
            default -> log.warn("알 수 없는 채팅 타입: {}", event.type());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional
    public void handleMessageRead(MessageReadEvent event) {
        entityManager.clear();

        switch (event.chatRoomType()) {
            case "DM" -> updateDmChatRoomList(event.chatRoomId(), event.userId(), null);
            case "GROUP" -> updateGroupChatRoomList(event.chatRoomId(), event.userId(), null);
            default -> log.warn("알 수 없는 채팅 타입: {}", event.chatRoomType());
        }
    }

    private void updateDmChatRoomList(Long chatRoomId, Long targetUserId, LocalDateTime updateTime) {
        // DM 채팅방의 모든 참가자들 조회
        List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom_Id(chatRoomId);

        // 모든 참가자에게 알림 전송 (읽음 처리한 사용자 포함)
        participants.forEach(participant -> {
            User receiver = participant.getUser();

            // 수신자 관점에서의 DM 채팅방 정보 생성 (읽음 상태 자동 계산됨)
            DmChatRoomListResponse updatedChatRoom = createDmChatRoomResponse(
                    chatRoomId, receiver);

            // WebSocket으로 수신자에게 전송
            ChatRoomListUpdateDto updateDto = ChatRoomListUpdateDto.fromDm(
                    updatedChatRoom,
                    LocalDateTime.now() // 읽음 처리 시간
            );

            messagingTemplate.convertAndSendToUser(
                    receiver.getUserId().toString(),
                    "/queue/chatroom-list-update",
                    updateDto
            );

            log.debug("DM 읽음 처리 업데이트 알림 전송: receiverId={}, chatRoomId={}, targetUserId={}",
                    receiver.getUserId(), chatRoomId, targetUserId);
        });
    }

    private void updateGroupChatRoomList(Long chatRoomId, Long otherUserId, LocalDateTime updateTime) {
//        // 그룹 채팅방의 모든 참가자들 조회
//        List<GroupUserChatRoom> participants = groupUserChatroomRepository.findByGroupChatRoomId(chatRoomId);
//
//        // 제외할 사용자를 제외한 모든 참가자에게 알림
//        participants.stream()
//                .filter(participant -> !participant.getUser().getUserId().equals(otherUserId))
//                .forEach(participant -> {
//                    User receiver = participant.getUser();
//
//                    // 수신자 관점에서의 그룹 채팅방 정보 생성
//                    GroupChatRoomListResponse updatedChatRoom = createGroupChatRoomResponse(
//                            chatRoomId, receiver);
//
//                    // WebSocket으로 수신자에게 전송
//                    ChatRoomListUpdateDto updateDto = ChatRoomListUpdateDto.fromMeeting(
//                            updatedChatRoom,
//                            updateTime
//                    );
//
//                    messagingTemplate.convertAndSendToUser(
//                            receiver.getUserId().toString(),
//                            "/queue/chatroom-list-update",
//                            updateDto
//                    );
//
//                    log.debug("그룹 채팅방 목록 업데이트 알림 전송: receiverId={}, chatRoomId={}, otherUserId={}",
//                            receiver.getUserId(), chatRoomId, otherUserId);
//                });
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

//    private Long getCurrentUserLastReadMessageIdForGroup(Long userId, Long chatRoomId) {
//        return groupUserChatroomRepository.findByUserUserIdAndGroupChatRoomId(userId, chatRoomId)
//                .map(GroupUserChatRoom::getLastReadMessageId)
//                .orElse(0L);
//    }
//
//    private long getLatestMessageIdForGroup(Long chatRoomId) {
//        return groupMessageRepository.findTopByGroupChatRoomIdOrderByCreatedAtDesc(chatRoomId)
//                .map(GroupMessage::getId)
//                .orElse(0L);
//    }
}