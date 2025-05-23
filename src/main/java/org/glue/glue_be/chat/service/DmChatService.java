package org.glue.glue_be.chat.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.DmChatRoomCreateRequest;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.chat.exception.ChatException;
import org.glue.glue_be.chat.repository.dm.DmChatRoomRepository;
import org.glue.glue_be.chat.repository.dm.DmMessageRepository;
import org.glue.glue_be.chat.repository.dm.DmUserChatroomRepository;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.dto.UserSummaryWithHostInfo;
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.glue.glue_be.chat.mapper.DmResponseMapper;

import java.util.*;
import java.util.stream.Collectors;
import org.glue.glue_be.util.fcm.service.FcmService;


@Service
@RequiredArgsConstructor
public class DmChatService extends CommonChatService {

    final int INVITE_AVAILABLE = 1;
    final int INVITE_NOT_NECESSARY = -1;

    private final DmChatRoomRepository dmChatRoomRepository;
    private final DmUserChatroomRepository dmUserChatroomRepository;
    private final DmMessageRepository dmMessageRepository;
    private final DmResponseMapper responseMapper;
    private final InvitationRepository invitationRepository;
    private final FcmService fcmService;

    // ===== 채팅방 생성 =====
    @Transactional
    public DmChatRoomCreateResult createDmChatRoom(DmChatRoomCreateRequest request, Long userId) {
        // 현재 사용자 조회, 미팅 ID 추출, 사용자 ID 목록 추출
        User currentUser = getUserById(userId);
        Long meetingId = request.getMeetingId();
        List<Long> userIds = request.getUserIds();

        // 사용자가 정확히 두 명인지 체크
        validateChatRoomUsers(userIds, userId);

        // 미팅 조회
        Meeting meeting = getMeetingById(meetingId);

        // 기존 채팅방 검색 - 있으면 바로 반환
        Optional<DmChatRoom> existingChatRoom = dmChatRoomRepository.findDirectChatRoomByUserIds(meetingId, userIds.get(0), userIds.get(1));
        if (existingChatRoom.isPresent()) {
            return new DmChatRoomCreateResult(
                    getDmChatRoomDetail(existingChatRoom.get().getId()),
                    new ActionResponse(200, "이미 존재하는 채팅방을 반환합니다.")
            );
        }

        // 새 채팅방 생성
        DmChatRoom chatRoom = dmChatRoomRepository.save(
                DmChatRoom.builder().meeting(meeting).build()
        );

        // 사용자 추가
        for (Long participantId : userIds) {
            User participant = getUserById(participantId);
            DmUserChatroom userChatroom = DmUserChatroom.builder()
                    .user(participant)
                    .dmChatRoom(chatRoom)
                    .build();
            chatRoom.addUserChatroom(userChatroom);
            dmUserChatroomRepository.save(userChatroom);
        }

        // 결과 반환
        return new DmChatRoomCreateResult(
                getDmChatRoomDetail(chatRoom.getId()),
                new ActionResponse(201, "채팅방을 성공적으로 생성하였습니다.")
        );
    }
    // =====


    // ===== DM방 정보 상세 조회 =====
    // 순수 채팅방 정보를 반환
    private DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId) {
        return getDmChatRoomDetail(dmChatRoomId, Optional.empty());
    }

    // 사용자별 DM방 정보 필요할 때 (사용자별 초대 여부, 사용자별 알림 토글 설정)
    @Transactional(readOnly = true)
    public DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId, Optional<Long> userId) {
        // 채팅방 ID로 dm방 조회
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);

        // 해당 dm방의 참여자 목록 조회
        List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(dmChatRoom);

        // 초대장 로직 - 사용자가 로그인한 경우에만 초대 정보 확인
        // "모임 초대하기"가 아예 필요 없는 상황
        Integer invitationStatus = INVITE_NOT_NECESSARY;
        if (userId.isPresent()) {
            invitationStatus = checkInvitationStatus(dmChatRoom, participants, userId.get());
        }

        // 아직 초대장이 한 번도 안 만들어진 상태일 때: 무조건 초대 가능하도록
        if (invitationStatus == null) {
            invitationStatus = INVITE_AVAILABLE;
        }

        // 응답 생성
        DmChatRoomDetailResponse responseWithoutHostInfo = responseMapper.toChatRoomDetailResponse(
                dmChatRoom, participants, userId.orElse(null), invitationStatus);

        // 호스트 정보를 추가한 참가자 목록 생성
        List<UserSummaryWithHostInfo> participantsWithHostInfo = participants.stream()
                .map(participant -> {
                    UserSummary userSummary = responseMapper.toChatUserResponse(participant.getUser());
                    boolean isHost = determineIsHost(
                            participant,
                            DmUserChatroom::getUser,
                            DmUserChatroom::getDmChatRoom,
                            DmChatRoom::getMeeting,
                            Meeting::getHost
                    );
                    return UserSummaryWithHostInfo.from(userSummary, isHost);
                })
                .collect(Collectors.toList());

        // 최종 응답 생성 (호스트 정보를 포함한 참가자 목록으로 업데이트)
        return responseWithoutHostInfo.toBuilder()
                .participantsWithHostInfo(participantsWithHostInfo)
                .build();
    }

    // 초대장 상태 체크
    private Integer checkInvitationStatus(DmChatRoom dmChatRoom, List<DmUserChatroom> participants, Long userId) {
        // 1. 모임 id 추출
        Long meetingId = dmChatRoom.getMeeting().getMeetingId();

        // 2. 로그인한 사용자 == 모임의 호스트인지 확인
        boolean isHost = dmChatRoom.getMeeting().getHost().getUserId().equals(userId);

        // 2-1. !isHost일 경우 초대 상태를 확인할 필요 없음
        if (!isHost) {
            return INVITE_NOT_NECESSARY;
        }

        // 3. 참여자 중 로그인한 사용자 빼고 추출
        Optional<Long> otherParticipantUserId = participants.stream()
                .map(duc -> duc.getUser().getUserId())
                .filter(id -> !id.equals(userId))
                .findFirst();

        // 3-1. 다른 참여자가 없으면 초대 상태를 확인할 필요 없음
        if (otherParticipantUserId.isEmpty()) {
            return INVITE_NOT_NECESSARY;
        }

        // 4. 초대 상태 직접 조회
        return invitationRepository.findStatusByMeetingAndParticipantIds(
                meetingId, userId, otherParticipantUserId.get());
    }
    // =====


    // ===== 채팅방 알림 상태 토글 =====
    @Transactional
    public Integer toggleDmPushNotification(Long dmChatRoomId, Long userId) {
        return processTogglePushNotification(
                dmChatRoomId,
                userId,
                this::getChatRoomById,                   // 채팅방 조회
                this::getUserById,                       // 사용자 조회
                this::validateChatRoomMember,            // 채팅방 멤버 검증
                DmUserChatroom::getPushNotificationOn,   // 현재 알림 상태 조회
                DmUserChatroom::togglePushNotification,  // 알림 상태 업데이트
                dmUserChatroomRepository::save           // 업데이트된 항목 저장
        );
    }
    // =====


    // === 내가 호스트/참석자인 DM 채팅방 목록 조회 ===
    // 내가 호스트인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getHostedDmChatRooms(Long userId) {
        return getChatRooms(
                userId,
                this::getUserById,
                dmChatRoomRepository::findByHost,
                this::convertToChatRoomResponses
        );
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getParticipatedDmChatRooms(Long userId) {
        return getChatRooms(
                userId,
                this::getUserById,
                user -> {
                    // 참가한 DM 채팅방들을 직접 조회
                    List<DmChatRoom> participatedDmChatRooms = dmUserChatroomRepository.findDmChatRoomsByUserId(userId);

                    // 내가 호스트가 아닌 미팅의 DM 채팅방만 필터링
                    return participatedDmChatRooms.stream()
                            .filter(dmChatRoom -> !dmChatRoom.getMeeting().getHost().getUserId().equals(userId))
                            .collect(Collectors.toList());
                },
                this::convertToChatRoomResponses
        );
    }

    // 채팅방 목록을 응답 객체로 변환
    private List<DmChatRoomListResponse> convertToChatRoomResponses(List<DmChatRoom> chatRooms, User currentUser) {
        return chatRooms.stream()
                .map(chatRoom -> {
                    List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(chatRoom);

                    User otherUser = participants.stream()
                            .map(DmUserChatroom::getUser)
                            .filter(user -> !user.getUserId().equals(currentUser.getUserId()))
                            .findFirst()
                            .orElse(currentUser);

                    DmMessage lastMessage = dmMessageRepository.findTopByDmChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    boolean hasUnreadMessages = dmMessageRepository.existsByDmChatRoomAndUser_UserIdNotAndIsRead(
                            chatRoom, currentUser.getUserId(), 0);

                    return responseMapper.toChatRoomListResponse(
                            chatRoom, otherUser, lastMessage, hasUnreadMessages);
                })
                .collect(Collectors.toList());
    }
    // =====


    // ===== DM방 나가기 =====
    // DM방 나가기
    @Transactional
    public List<ActionResponse> leaveDmChatRoom(Long dmChatRoomId, Long userId) {
        return (List<ActionResponse>) processLeaveChatRoom(
                dmChatRoomId,
                userId,
                this::getChatRoomById,
                this::getUserById,
                this::validateChatRoomMember,
                dmUserChatroomRepository::deleteByDmChatRoomAndUser,
                dmUserChatroomRepository::findByDmChatRoom,
                dmMessageRepository::findByDmChatRoomOrderByCreatedAtAsc,
                dmMessageRepository::deleteAll,
                dmChatRoomRepository::delete
        );
    }
    // =====


    // ===== DM방 진입 시, 대화 이력 조회 + 안 읽었던 것들 읽음 처리(실시간+비실시간) =====
    // 대화 이력 조회 후 읽지 않은 메시지 읽음 처리
    @Transactional
    public List<DmMessageResponse> getDmMessagesByDmChatRoomId(Long dmChatRoomId, Long userId) {
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);
        User user = getUserById(userId);
        validateChatRoomMember(dmChatRoom, user);

        // 읽지 않은 메시지 읽음 처리
        List<DmMessage> messages = dmMessageRepository.findByDmChatRoomOrderByCreatedAtAsc(dmChatRoom);
        markMessagesAsRead(dmChatRoomId, userId);

        return messages.stream()
                .map(responseMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    // 안 읽었던 것들 읽음 처리하는 실시간+비실시간 로직을 공통 매소드에 전달
    @Transactional
    public void markMessagesAsRead(Long dmChatRoomId, Long userId) {
        processMarkAsRead(
                dmChatRoomId,
                userId,
                this::getChatRoomById,
                this::validateChatRoomMember,
                dmMessageRepository::findUnreadMessages,
                message -> message.setIsRead(1),
                dmMessageRepository::saveAll,
                this::notifyDmMessageRead
        );
    }

    // 웹소켓에 연결되어 있을 때 메시지를 읽었다는 정보를 실시간으로 전달
    private void notifyDmMessageRead(Long dmChatRoomId, Long receiverId, List<DmMessage> readMessages) {
        if (!readMessages.isEmpty()) {
            DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(dmChatRoomId);

            List<DmMessageResponse> updatedMessages = readMessages.stream()
                    .map(responseMapper::toMessageResponse)
                    .collect(Collectors.toList());

            DmReadStatusUpdateResponse readStatus = new DmReadStatusUpdateResponse(
                    dmChatRoomId, receiverId, updatedMessages
            );

            for (UserSummary participant : chatRoom.getParticipants()) {
                if (!participant.getUserId().equals(receiverId)) {
                    sendNotificationToUser(participant.getUserId(), "dm/read", readStatus);
                }
            }
        }
    }
    // =====


    // ===== DM 전송 =====
    // DM 전송 총괄 매소드
    @Transactional
    public DmMessageResponse processDmMessage(Long dmChatRoomId, DmMessageSendRequest request, Long userId) {
        // 1. 메시지 저장
        DmMessageResponse response = saveDmMessage(dmChatRoomId, userId, request.getContent());

        // 2. 메시지 전송 관리(온라인-웹소켓, 오프라인-푸시알림)
        broadcastMessage(dmChatRoomId, response, userId);

        return response;
    }

    // DB에 전송한 메시지 저장
    private DmMessageResponse saveDmMessage(Long dmChatRoomId, Long senderId, String content) {
        return saveMessage(
                dmChatRoomId,
                senderId,
                content,
                this::getChatRoomById,                           // 채팅방 찾기
                this::validateChatRoomMember,                    // 멤버십 검증
                (chatRoom, sender, messageContent) ->            // 메시지 생성
                        DmMessage.builder()
                                .chatRoom(chatRoom)
                                .user(sender)
                                .dmMessageContent(messageContent)
                                .build(),
                dmMessageRepository::save,                       // 메시지 저장
                responseMapper::toMessageResponse                // 응답 매핑
        );
    }

    // 알림 전송: 웹소켓(실시간) + 푸시(비실시간)
    private void broadcastMessage(Long dmChatRoomId, DmMessageResponse messageResponse, Long senderId) {
        // 채팅방 정보 가져오기
        DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(dmChatRoomId);

        // 1. 모든 참여자에게 웹소켓으로 메시지 전송 시도 (온라인 상태인 참여자만 받게 됨)
        // sendWebSocketMessageToOnlineReceivers 내부의 convertAndSend() 매소드가 연결 상태를 자체적으로 확인한다!
        sendWebSocketMessageToOnlineReceivers(
                chatRoom.getParticipants(),
                senderId,
                "/queue/dm/",
                messageResponse,
                UserSummary::getUserId
        );

        // 2. 오프라인 참여자에게 푸시 알림 전송
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);
        DmMessage message = dmMessageRepository.findById(messageResponse.getDmMessageId())
                .orElseThrow(() -> new ChatException("메시지를 찾을 수 없습니다."));

        // 이 매소드 내부에서 조건 필터링
        sendPushNotificationsToOfflineReceivers(
                message,
                dmChatRoom,
                senderId,
                "/queue/dm",
                DmMessage::getDmMessageContent,                      // 메시지 내용 추출
                DmMessage::getUser,                                  // 발신자 정보 추출
                dmUserChatroomRepository::findByDmChatRoom,          // 참여자 목록 조회
                DmUserChatroom::getUser,                             // 사용자 정보 추출
                DmUserChatroom::getPushNotificationOn,               // 알림 설정 조회
                this::isUserConnectedToWebSocket,                    // 웹소켓 연결 확인
                (sender, recipient, content) -> FcmSendDto.builder() // 알림 객체 생성
                        .title(sender.getNickname() + "님의 메시지")
                        .body(content)
                        .token(recipient.getFcmToken())
                        .build(),
                fcmService::sendMessage                              // 알림 전송
        );
    }
    // =====


    // ===== 공통 =====
    private DmChatRoom getChatRoomById(Long dmChatRoomId) {
        return dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));
    }

    private DmUserChatroom validateChatRoomMember(DmChatRoom chatRoom, User user) {
        return dmUserChatroomRepository.findByDmChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new ChatException("채팅방에 참여하지 않은 사용자입니다."));
    }

    @Override
    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // DM 특화 검증: 2명 참여 확인 및 현재 사용자 포함 확인
        if (userIds.size() != 2 || !userIds.contains(currentUserId)) {
            throw new ChatException("DM 채팅방은 본인을 포함한 정확히 2명의 사용자가 필요합니다.");
        }
    }
}