package org.glue.glue_be.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.request.GroupMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
import org.glue.glue_be.chat.event.MessageCreatedEvent;
import org.glue.glue_be.chat.event.MessageReadEvent;
import org.glue.glue_be.chat.mapper.GroupResponseMapper;
import org.glue.glue_be.chat.repository.group.GroupChatRoomRepository;
import org.glue.glue_be.chat.repository.group.GroupMessageRepository;
import org.glue.glue_be.chat.repository.group.GroupUserChatRoomRepository;
import org.glue.glue_be.common.dto.UserSummary;
import org.glue.glue_be.common.dto.UserSummaryWithHostInfo;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.common.response.BaseResponseStatus;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.service.FcmService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroupChatService extends CommonChatService {

    private final GroupChatRoomRepository groupChatRoomRepository;
    private final GroupUserChatRoomRepository groupUserChatRoomRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final GroupResponseMapper responseMapper;
    private final FcmService fcmService;

    private final ApplicationEventPublisher eventPublisher;


    // ===== 그룹 채팅방 생성 =====
    @Transactional
    public GroupChatRoomCreateResult createGroupChatRoom(Long meetingId, Long userId) {
        try {
            // 미팅 정보 조회
            Meeting meeting = getMeetingById(meetingId);
            if (meeting == null) {
                throw new BaseException(ChatResponseStatus.MEETING_NOT_FOUND);
            }

            User user = getUserById(userId);

            // 기존에 해당 미팅에 대한 그룹 채팅방이 있는지 여부에 따라 로직 처리가 다르게 됨
            if (groupChatRoomRepository.existsByMeeting_MeetingId(meetingId)) {
                // 기존 채팅방이 있으면 사용자만 추가
                GroupChatRoom chatRoom = groupChatRoomRepository
                    .findFirstByMeeting_MeetingId(meetingId)
                    .orElseThrow(() -> new BaseException(ChatResponseStatus.CHATROOM_NOT_FOUND));

                // 이미 사용자가 채팅방에 참여 중인지 확인
                Optional<GroupUserChatRoom> existingUserChatroom =
                        groupUserChatRoomRepository.findByGroupChatroomAndUser(chatRoom, user);

                if (existingUserChatroom.isPresent()) {
                    // 이미 참여 중이면 기존 채팅방 정보 반환
                    return new GroupChatRoomCreateResult(
                            getGroupChatRoomDetail(chatRoom.getGroupChatroomId(), userId),
                            new ActionResponse(ChatResponseStatus.CHATROOM_JOINED.getCode(), ChatResponseStatus.CHATROOM_JOINED.getMessage())
                    );
                } else {
                    // 채팅방에 사용자 추가
                    GroupUserChatRoom groupUserChatRoom = new GroupUserChatRoom(user, chatRoom);
                    groupUserChatRoomRepository.save(groupUserChatRoom);

                    return new GroupChatRoomCreateResult(
                            getGroupChatRoomDetail(chatRoom.getGroupChatroomId(), userId),
                            new ActionResponse(ChatResponseStatus.CHATROOM_JOINED.getCode(), ChatResponseStatus.CHATROOM_JOINED.getMessage())
                    );
                }
            } else {
                // 새 채팅방 생성 시 호스트 권한 확인
                if (!user.getUserId().equals(meeting.getHost().getUserId())) {
                    throw new BaseException(ChatResponseStatus.ONLY_HOST_CAN_CREATE);
                }

                // 새 채팅방 생성
                GroupChatRoom newChatRoom = new GroupChatRoom(meeting);
                GroupChatRoom savedChatRoom = groupChatRoomRepository.save(newChatRoom);

                // 채팅방에 사용자 추가
                GroupUserChatRoom groupUserChatRoom = new GroupUserChatRoom(user, savedChatRoom);
                groupUserChatRoomRepository.save(groupUserChatRoom);

                return new GroupChatRoomCreateResult(
                        getGroupChatRoomDetail(savedChatRoom.getGroupChatroomId(), userId),
                        new ActionResponse(ChatResponseStatus.CHATROOM_CREATED.getCode(), ChatResponseStatus.CHATROOM_CREATED.getMessage())
                );
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.CHATROOM_CREATION_FAILED);
        }
    }
    // =====


    // ===== 그룹 채팅방 상세 정보 조회 =====
    @Transactional(readOnly = true)
    public GroupChatRoomDetailResponse getGroupChatRoomDetail(Long groupChatroomId, Long userId) {
        try {
            // 채팅방 조회
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);

            // 채팅방 참여자 목록 조회
            List<GroupUserChatRoom> participants = getChatRoomParticipants(groupChatRoom);

            // 요청한 사용자가 채팅방 참여자인지 확인
            boolean isParticipant = participants.stream()
                    .anyMatch(participant -> participant.getUser().getUserId().equals(userId));

            if (!isParticipant) {
                throw new BaseException(ChatResponseStatus.USER_NOT_MEMBER);
            }

            // 응답 생성
            GroupChatRoomDetailResponse responseWithoutHostInfo = responseMapper.toChatRoomDetailResponse(
                    groupChatRoom, participants, userId);

            // 호스트 정보를 추가한 참가자 목록 생성
            List<UserSummaryWithHostInfo> participantsWithHostInfo = participants.stream()
                    .map(participant -> {
                        UserSummary userSummary = responseMapper.toChatUserResponse(participant.getUser());
                        boolean isHost = determineIsHost(
                                participant,
                                GroupUserChatRoom::getUser,
                                GroupUserChatRoom::getGroupChatroom,
                                GroupChatRoom::getMeeting,
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
    // =====


    // ===== 채팅방 알림 상태 토글 =====
    @Transactional
    public Integer toggleGroupPushNotification(Long groupChatroomId, Long userId) {
        return processTogglePushNotification(
                groupChatroomId,
                userId,
                this::getChatRoomById,                       // 채팅방 조회
                this::getUserById,                           // 사용자 조회
                this::validateChatRoomMember,                // 채팅방 멤버 검증
                GroupUserChatRoom::getPushNotificationOn,    // 현재 알림 상태 조회
                GroupUserChatRoom::togglePushNotification,   // 알림 상태 업데이트
                groupUserChatRoomRepository::save            // 업데이트된 항목 저장
        );
    }
    // =====


    // ===== 내가 참여 중인 그룹 채팅방 목록 조회 =====
    @Transactional(readOnly = true)
    public List<GroupChatRoomListResponse> getGroupChatRooms(Long cursorId, Integer pageSize, Long userId) {
        try {
            return getChatRooms(
                    cursorId,
                    pageSize,
                    userId,
                    this::getUserById,
                    groupChatRoomRepository::findByUserOrderByGroupChatroomUpdatedAtDesc,
                    groupChatRoomRepository::findByUserAndGroupChatroomUpdatedAtLessThanOrderByGroupChatroomIdDesc,
                    this::convertToGroupChatRoomResponses
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(BaseResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<GroupChatRoomListResponse> convertToGroupChatRoomResponses(List<GroupChatRoom> chatRooms, User currentUser) {
        return chatRooms.stream()
                .map(chatRoom -> {
                    // 참여자 수 계산
                    List<GroupUserChatRoom> participants = getChatRoomParticipants(chatRoom);
                    int participantCount = participants.size();

                    // 최근 메시지 조회
                    GroupMessage lastMessage = groupMessageRepository.findTopByGroupChatroomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    // 현재 사용자의 마지막 읽은 메시지 ID 조회
                    Long currentUserLastReadMessageId = getCurrentUserLastReadMessageId(currentUser.getUserId(), chatRoom.getGroupChatroomId());

                    // 채팅방의 가장 최신 메시지 ID 조회
                    long latestMessageId = lastMessage != null ? lastMessage.getGroupMessageId() : 0L;

                    // 읽지 않은 메시지 여부 확인
                    boolean hasUnreadMessages = currentUserLastReadMessageId < latestMessageId;

                    return responseMapper.toChatRoomListResponse(
                            chatRoom, lastMessage, hasUnreadMessages, participantCount);
                })
                .collect(Collectors.toList());
    }
    // =====


    // ===== 그룹 채팅방 나가기 =====
    @Transactional
    public List<ActionResponse> leaveGroupChatRoom(Long groupChatroomId, Long userId) {
        try {
            GroupChatRoom chatRoom = getChatRoomById(groupChatroomId);
            User user = getUserById(userId);

            Meeting meeting = chatRoom.getMeeting();

            if (meeting != null && user.getUserId().equals(meeting.getHost().getUserId())) {
                throw new BaseException(ChatResponseStatus.HOST_CANNOT_LEAVE);
            }

            return (List<ActionResponse>) processLeaveChatRoom(
                    groupChatroomId,
                    userId,
                    this::getChatRoomById,
                    this::getUserById,
                    this::validateChatRoomMember,
                    groupUserChatRoomRepository::deleteByGroupChatroomAndUser,
                    groupUserChatRoomRepository::findByGroupChatroom,
                    groupMessageRepository::findByGroupChatroomOrderByCreatedAtAsc,
                    groupMessageRepository::deleteAll,
                    groupChatRoomRepository::delete
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
    public List<GroupMessageResponse> getGroupMessagesByGroupChatRoomId(Long groupChatroomId, Long cursorId, Integer pageSize, Long userId) {
        try {
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);
            User user = getUserById(userId);
            validateChatRoomMember(groupChatRoom, user);

            return getMessagesWithPagination(
                    cursorId,
                    pageSize,
                    groupChatRoom,
                    groupMessageRepository::findByGroupChatroomOrderByGroupMessageIdDesc,
                    groupMessageRepository::findByGroupChatroomAndGroupMessageIdLessThanOrderByGroupMessageIdDesc,
                    responseMapper::toMessageResponse,
                    () -> markMessagesAsRead(groupChatroomId, userId)
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_NOT_FOUND);
        }
    }

    // 안 읽었던 것들 읽음 처리하는 실시간+비실시간 로직을 공통 매소드에 전달
    @Transactional
    public void markMessagesAsRead(Long groupChatroomId, Long userId) {
        try {
            processMarkAsRead(
                    groupChatroomId,
                    userId,
                    this::getChatRoomById,
                    this::validateChatRoomMember,
                    this::getLatestMessageIdInChatRoom,
                    groupUserChatRoomRepository::updateLastReadMessageId
            );

            eventPublisher.publishEvent(new MessageReadEvent(userId, groupChatroomId, "GROUP"));

        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGES_READ_FAILED);
        }
    }

    // 메시지 읽음 알림 전송(푸시 알림 아님, 실시간 알림)
    // TODO: 레디스 도입하며 아마 함께 사용하게 될 메소드 (현재 사용 x)
    private void notifyGroupMessageRead(Long groupChatroomId, Long receiverId, List<GroupMessage> readMessages) {
        if (!readMessages.isEmpty()) {
            GroupChatRoomDetailResponse chatRoom = getGroupChatRoomDetail(groupChatroomId, receiverId);

            List<GroupMessageResponse> updatedMessages = readMessages.stream()
                    .map(responseMapper::toMessageResponse)
                    .collect(Collectors.toList());

            GroupReadStatusUpdateResponse readStatus = new GroupReadStatusUpdateResponse(
                    groupChatroomId, receiverId, updatedMessages
            );

            // 모든 참여자에게 알림 전송 (읽은 사용자 제외)
            for (UserSummary participant : chatRoom.participants()) {
                if (!participant.getUserId().equals(receiverId)) {
                    sendNotificationToUser(participant.getUserId(), "group/read", readStatus);
                }
            }
        }
    }
    // =====


    // ===== 그룹 메시지 전송 =====
    @Transactional
    public GroupMessageResponse processGroupMessage(Long groupChatroomId, GroupMessageSendRequest request, Long userId) {
        try {
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);
            User sender = getUserById(userId);
            validateChatRoomMember(groupChatRoom, sender);

            // 메시지 db에 저장
            GroupMessageResponse response = saveGroupMessage(groupChatRoom, sender, request.content());

            // 채팅방 목록 업데이트 이벤트 발행
            publishChatRoomListUpdateEvent(response, groupChatroomId, userId);

            // 메시지 전송 및 알림
            // 온라인: 웹소켓으로, 오프라인: 푸시알림으로
            broadcastMessage(groupChatroomId, response, userId);

            return response;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // 메시지 db에 저장
    private GroupMessageResponse saveGroupMessage(GroupChatRoom chatRoom, User sender, String content) {
        try {
            // 메시지 생성 및 저장
            GroupMessage message = new GroupMessage(
                    sender,
                    chatRoom,
                    chatRoom.getMeeting(),
                    content
            );

            GroupMessage savedMessage = groupMessageRepository.save(message);
            chatRoom.updateLastActivity();

            return responseMapper.toMessageResponse(savedMessage);
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // 채팅방 목록 업데이트 이벤트 발행
    private void publishChatRoomListUpdateEvent(GroupMessageResponse messageResponse, Long chatRoomId, Long senderId) {
        eventPublisher.publishEvent(new MessageCreatedEvent(
                messageResponse.groupMessageId(),
                chatRoomId,
                senderId,
                messageResponse.message(),
                messageResponse.createdAt(),
                "GROUP"
        ));
    }

    // 온라인 유저에게 메시지 웹소켓으로 브로드캐스트, 오프라인 유저에겐 푸시 알림 전송
    private void broadcastMessage(Long groupChatroomId, GroupMessageResponse messageResponse, Long senderId) {
        try {
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);
            Optional<GroupMessage> messageOpt = groupMessageRepository.findById(messageResponse.groupMessageId());

            if (messageOpt.isEmpty()) {
                throw new BaseException(ChatResponseStatus.MESSAGE_NOT_FOUND);
            }

            GroupMessage message = messageOpt.get();
            GroupChatRoomDetailResponse chatRoom = getGroupChatRoomDetail(groupChatroomId, senderId);

            // 모든 참여자에게 웹소켓 전송
            // 웹소켓에 연결된 사용자만 실제 수신
            // 오프라인 수신자는 받지도 않고 에러를 내지도 않고 그냥 무시
            for (UserSummary participant : chatRoom.participants()) {
                Long participantId = participant.getUserId();

                if (!participantId.equals(senderId)) {
                    messagingTemplate.convertAndSend("/topic/group/" + participantId, messageResponse);
                }
            }

            // 모든 오프라인 참여자에게 푸시 알림 전송
            sendPushNotificationsToOfflineReceivers(
                    message,
                    groupChatRoom,
                    senderId,
                    "/topic/group",
                    GroupMessage::getMessage,
                    GroupMessage::getUser,
                    groupUserChatRoomRepository::findByGroupChatroom,
                    GroupUserChatRoom::getUser,
                    GroupUserChatRoom::getPushNotificationOn,
                    this::isUserConnectedToWebSocket,
                    (sender, recipient, content) -> FcmSendDto.builder()
                            .title(sender.getNickname() + "님의 그룹 메시지")
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


    // ===== 공통 메소드 =====
    private GroupChatRoom getChatRoomById(Long groupChatroomId) {
        return groupChatRoomRepository.findById(groupChatroomId)
                .orElseThrow(() -> new BaseException(ChatResponseStatus.CHATROOM_NOT_FOUND));
    }

    private GroupUserChatRoom validateChatRoomMember(GroupChatRoom chatRoom, User user) {
        return groupUserChatRoomRepository.findByGroupChatroomAndUser(chatRoom, user)
                .orElseThrow(() -> new BaseException(ChatResponseStatus.USER_NOT_MEMBER));
    }

    private List<GroupUserChatRoom> getChatRoomParticipants(GroupChatRoom chatRoom) {
        return groupUserChatRoomRepository.findByGroupChatroom(chatRoom);
    }

    @Override
    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // 그룹 채팅방은 검증 로직이 필요 없음 (DM과 달리 참여자 수 제한이 없음)
        // 단, 현재 사용자가 포함되어 있는지는 확인
        if (!userIds.contains(currentUserId)) {
            throw new BaseException(ChatResponseStatus.INVALID_GROUP_USER);
        }
    }

    private Long getLatestMessageIdInChatRoom(GroupChatRoom chatroom) {
        return groupMessageRepository.findTopByGroupChatroomOrderByCreatedAtDesc(chatroom)
                .map(GroupMessage::getGroupMessageId)
                .orElse(0L);
    }

    private Long getCurrentUserLastReadMessageId(Long userId, Long chatroomId) {
        return groupUserChatRoomRepository
                .findByUser_UserIdAndGroupChatroom_GroupChatroomId(userId, chatroomId)
                .map(GroupUserChatRoom::getLastReadMessageId)
                .orElse(0L);
    }

    // =====
}