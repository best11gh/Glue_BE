package org.glue.glue_be.chat.service;

import lombok.RequiredArgsConstructor;
import org.glue.glue_be.chat.dto.request.DmChatRoomCreateRequest;
import org.glue.glue_be.chat.dto.request.DmMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.entity.dm.DmChatRoom;
import org.glue.glue_be.chat.entity.dm.DmMessage;
import org.glue.glue_be.chat.entity.dm.DmUserChatroom;
import org.glue.glue_be.chat.dto.response.ChatResponseStatus;
import org.glue.glue_be.chat.repository.dm.DmChatRoomRepository;
import org.glue.glue_be.chat.repository.dm.DmMessageRepository;
import org.glue.glue_be.chat.repository.dm.DmUserChatroomRepository;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.dto.UserSummaryWithHostInfo;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.common.response.BaseResponseStatus;
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.meeting.entity.Meeting;
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
        try {
            // 현재 사용자 조회, 미팅 ID 추출, 사용자 ID 목록 추출
            User currentUser = getUserById(userId);
            Long meetingId = request.getMeetingId();
            List<Long> userIds = request.getUserIds();

            // 사용자가 정확히 두 명인지 체크
            validateChatRoomUsers(userIds, userId);

            // 미팅 조회
            Meeting meeting = getMeetingById(meetingId);
            if (meeting == null) {
                throw new BaseException(ChatResponseStatus.CHATROOM_CREATION_FAILED_NO_MEETING);
            }

            // 기존 채팅방 검색 - 있으면 바로 반환
            Optional<DmChatRoom> existingChatRoom = dmChatRoomRepository.findDirectChatRoomByUserIds(meetingId, userIds.get(0), userIds.get(1));
            if (existingChatRoom.isPresent()) {
                return new DmChatRoomCreateResult(
                        getDmChatRoomDetail(existingChatRoom.get().getId()),
                        new ActionResponse(ChatResponseStatus.CHATROOM_FOUND.getCode(), ChatResponseStatus.CHATROOM_FOUND.getMessage())
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
                    new ActionResponse(ChatResponseStatus.CHATROOM_CREATED.getCode(), ChatResponseStatus.CHATROOM_CREATED.getMessage())
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.CHATROOM_CREATION_FAILED);
        }
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
        try {
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
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 초대장 상태 체크
    private Integer checkInvitationStatus(DmChatRoom dmChatRoom, List<DmUserChatroom> participants, Long userId) {
        try {
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
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.INVITATION_STATUS_ERROR);
        }
    }
    // =====


    // ===== 채팅방 알림 상태 토글 =====
    @Transactional
    public Integer toggleDmPushNotification(Long dmChatRoomId, Long userId) {
        try {
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
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.NOTIFICATION_TOGGLED_FAILED);
        }
    }
    // =====


    // === 내가 호스트/참석자인 DM 채팅방 목록 조회 ===
    // 내가 호스트인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getHostedDmChatRooms(Long cursorId, Integer pageSize, Long userId) {
        try {
            return getChatRooms(
                    cursorId,
                    pageSize,
                    userId,
                    this::getUserById,
                    dmChatRoomRepository::findByMeetingHostOrderByIdDesc,
                    dmChatRoomRepository::findByMeetingHostAndIdLessThanOrderByIdDesc,
                    this::convertToChatRoomResponses
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getParticipatedDmChatRooms(Long cursorId, Integer pageSize, Long userId) {
        try {
            return getChatRooms(
                    cursorId,
                    pageSize,
                    userId,
                    this::getUserById,
                    (user, pageable) -> dmUserChatroomRepository.findDmChatRoomsByUserOrderByDmChatRoomIdDesc(user.getUserId(), pageable),
                    (user, curId, pageable) -> dmUserChatroomRepository.findDmChatRoomsByUserAndDmChatRoomIdLessThanOrderByDmChatRoomIdDesc(user.getUserId(), curId, pageable),
                    (chatRooms, user) -> {
                        // 내가 호스트가 아닌 미팅의 DM 채팅방만 필터링 후 변환
                        List<DmChatRoom> filteredChatRooms = chatRooms.stream()
                                .filter(dmChatRoom -> !dmChatRoom.getMeeting().getHost().getUserId().equals(user.getUserId()))
                                .collect(Collectors.toList());

                        return convertToChatRoomResponses(filteredChatRooms, user);
                    }
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
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

                    // 현재 사용자의 마지막 읽은 메시지 ID 조회
                    Long currentUserLastReadMessageId = getCurrentUserLastReadMessageId(currentUser.getUserId(), chatRoom.getId());

                    // 채팅방의 가장 최신 메시지 ID 조회
                    long latestMessageId = getLatestMessageId(chatRoom);

                    // 읽지 않은 메시지 여부 확인
                    boolean hasUnreadMessages = currentUserLastReadMessageId < latestMessageId;

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
        try {
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
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }
    // =====


    // ===== DM방 진입 시, 대화 이력 조회 + 안 읽었던 것들 읽음 처리(실시간+비실시간) =====
    // 대화 이력 조회 후 읽지 않은 메시지 읽음 처리
    @Transactional
    public List<DmMessageResponse> getDmMessagesByDmChatRoomId(Long dmChatRoomId, Long cursorId, Integer pageSize, Long userId) {
        try {
            DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);
            User user = getUserById(userId);
            validateChatRoomMember(dmChatRoom, user);

            return getMessagesWithPagination(
                    cursorId,
                    pageSize,
                    dmChatRoom,
                    dmMessageRepository::findByDmChatRoomOrderByIdDesc,
                    dmMessageRepository::findByDmChatRoomAndIdLessThanOrderByIdDesc,
                    responseMapper::toMessageResponse,
                    () -> markMessagesAsRead(dmChatRoomId, userId)
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_NOT_FOUND);
        }
    }

    // 안 읽었던 것들 읽음 처리하는 실시간+비실시간 로직을 공통 매소드에 전달
    @Transactional
    public void markMessagesAsRead(Long dmChatRoomId, Long userId) {
        try {
            processMarkAsRead(
                    dmChatRoomId,
                    userId,
                    this::getChatRoomById,
                    this::validateChatRoomMember,
                    this::getLatestMessageId,
                    dmUserChatroomRepository::updateLastReadMessageId
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGES_READ_FAILED);
        }
    }

    // 메시지 읽음 알림 전송(푸시 알림 아님, 실시간 알림)
    // TODO: 레디스 도입하며 아마 함께 사용하게 될 메소드 (현재 사용 x)
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
        try {
            // 메시지 db에 저장
            DmMessageResponse response = saveDmMessage(dmChatRoomId, userId, request.getContent());

            // 메시지 전송 및 알림
            // 온라인: 웹소켓으로, 오프라인: 푸시알림으로
            broadcastMessage(dmChatRoomId, response, userId);

            return response;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // DB에 전송한 메시지 저장
    private DmMessageResponse saveDmMessage(Long dmChatRoomId, Long senderId, String content) {
        try {
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
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // 알림 전송: 웹소켓(실시간) + 푸시(비실시간)
    private void broadcastMessage(Long chatroomId, DmMessageResponse messageResponse, Long senderId) {
        try {
            DmChatRoom dmChatRoom = getChatRoomById(chatroomId);
            Optional<DmMessage> messageOpt = dmMessageRepository.findById(messageResponse.getDmMessageId());

            if (messageOpt.isEmpty()) {
                throw new BaseException(ChatResponseStatus.MESSAGE_NOT_FOUND);
            }

            DmMessage message = messageOpt.get();
            DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(chatroomId, Optional.ofNullable(senderId));

            // 모든 참여자에게 웹소켓 전송
            // 웹소켓에 연결된 사용자만 실제 수신
            // 오프라인 수신자는 받지도 않고 에러를 내지도 않고 그냥 무시
            for (UserSummary participant : chatRoom.getParticipants()) {
                Long participantId = participant.getUserId();

                if (!participantId.equals(senderId)) {
                    messagingTemplate.convertAndSend("/queue/dm/" + participantId, messageResponse);
                }
            }

            // 모든 오프라인 참여자에게 푸시 알림 전송
            sendPushNotificationsToOfflineReceivers(
                    message,
                    dmChatRoom,
                    senderId,
                    "/queue/dm",
                    DmMessage::getDmMessageContent,
                    DmMessage::getUser,
                    dmUserChatroomRepository::findByDmChatRoom,
                    DmUserChatroom::getUser,
                    DmUserChatroom::getPushNotificationOn,
                    this::isUserConnectedToWebSocket,
                    (sender, recipient, content) -> FcmSendDto.builder()
                            .title(sender.getNickname() + "님의 쪽지")
                            .body(content)
                            .token(recipient.getFcmToken())
                            .build(),
                    fcmService::sendMessage
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }
    // =====


    // ===== 공통 =====
    private DmChatRoom getChatRoomById(Long dmChatRoomId) {
        return dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new BaseException(ChatResponseStatus.CHATROOM_NOT_FOUND));
    }

    private DmUserChatroom validateChatRoomMember(DmChatRoom chatRoom, User user) {
        return dmUserChatroomRepository.findByDmChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new BaseException(ChatResponseStatus.USER_NOT_MEMBER));
    }

    private Long getCurrentUserLastReadMessageId(Long userId, Long chatroomId) {
        return dmUserChatroomRepository
                .findByUser_UserIdAndDmChatRoom_Id(userId, chatroomId)  // Repository 메소드명 수정
                .map(DmUserChatroom::getLastReadMessageId)              // getId() -> getLastReadMessageId()
                .orElse(0L);                                            // Optional 처리
    }

    private Long getLatestMessageId(DmChatRoom chatRoom) {
        DmMessage lastMessage = dmMessageRepository.findTopByDmChatRoomOrderByCreatedAtDesc(chatRoom)
                .orElse(null);
        return lastMessage != null ? lastMessage.getId() : 0L;
    }

    @Override
    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // DM 특화 검증: 2명 참여 확인 및 현재 사용자 포함 확인
        if (userIds.size() != 2 || !userIds.contains(currentUserId)) {
            throw new BaseException(ChatResponseStatus.INVALID_DM_USER_COUNT);
        }
    }
}