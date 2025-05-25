package org.glue.glue_be.chat.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.request.GroupMessageSendRequest;
import org.glue.glue_be.chat.dto.response.*;
import org.glue.glue_be.chat.entity.group.GroupChatRoom;
import org.glue.glue_be.chat.entity.group.GroupMessage;
import org.glue.glue_be.chat.entity.group.GroupUserChatRoom;
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

            // 기존에 해당 미팅에 대한 그룹 채팅방이 있는지 확인
            Optional<GroupChatRoom> existingChatRoom = groupChatRoomRepository.findByMeeting_MeetingId(meetingId);

            if (existingChatRoom.isPresent()) {
                // 기존 채팅방이 있으면 사용자만 추가
                GroupChatRoom chatRoom = existingChatRoom.get();

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
            List<GroupUserChatRoom> participants = groupUserChatRoomRepository.findByGroupChatroom(groupChatRoom);

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
                    groupChatRoomRepository::findByUserOrderByGroupChatroomIdDesc,
                    groupChatRoomRepository::findByUserAndGroupChatroomIdLessThanOrderByGroupChatroomIdDesc,
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
                    List<GroupUserChatRoom> participants = groupUserChatRoomRepository.findByGroupChatroom(chatRoom);
                    int participantCount = participants.size();

                    // 최근 메시지 조회
                    GroupMessage lastMessage = groupMessageRepository.findTopByGroupChatroomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    // 읽지 않은 메시지 여부 확인
                    boolean hasUnreadMessages = groupMessageRepository.existsByGroupChatroomAndUser_UserIdNotAndUnreadCount(
                            chatRoom, currentUser.getUserId(), 0);

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
                    this::getChatRoomById,                           // 채팅방 조회
                    this::validateChatRoomMember,                    // 채팅방 멤버 검증
                    groupMessageRepository::findUnreadMessages,      // 읽지 않은 메시지 조회
                    GroupMessage::updateUnreadCount,                 // 읽음 처리
                    groupMessageRepository::saveAll,                 // 업데이트된 메시지 저장
                    this::notifyGroupMessageRead                     // 읽음 알림 전송
            );
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGES_READ_FAILED);
        }
    }

    // 메시지 읽음 알림 전송
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
            // 메시지 저장
            GroupMessageResponse response = saveGroupMessage(groupChatroomId, userId, request.content());

            // 메시지 전송 및 알림
            broadcastMessage(groupChatroomId, response, userId);

            return response;
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // 메시지 저장
    private GroupMessageResponse saveGroupMessage(Long groupChatroomId, Long senderId, String content) {
        try {
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);
            User sender = getUserById(senderId);
            validateChatRoomMember(groupChatRoom, sender);

            // 메시지 생성
            GroupMessage groupMessage = new GroupMessage(
                    sender,
                    groupChatRoom,
                    groupChatRoom.getMeeting(),
                    content,
                    countOtherParticipants(groupChatRoom, senderId)
            );

            // 메시지 저장
            GroupMessage savedMessage = groupMessageRepository.save(groupMessage);

            // 응답 생성
            return responseMapper.toMessageResponse(savedMessage);
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
    }

    // 채팅방 참여자 수 계산 (발신자 제외)
    private Integer countOtherParticipants(GroupChatRoom groupChatRoom, Long senderId) {
        List<GroupUserChatRoom> participants = groupUserChatRoomRepository.findByGroupChatroom(groupChatRoom);
        return (int) participants.stream()
                .filter(p -> !p.getUser().getUserId().equals(senderId))
                .count();
    }

    // 메시지 브로드캐스트 및 알림 전송
    private void broadcastMessage(Long groupChatroomId, GroupMessageResponse messageResponse, Long senderId) {
        try {
            // 채팅방 정보 조회
            GroupChatRoomDetailResponse chatRoom = getGroupChatRoomDetail(groupChatroomId, senderId);

            // 1. 온라인 참여자에게 웹소켓으로 메시지 전송
            sendWebSocketMessageToOnlineReceivers(
                    chatRoom.participants(),
                    senderId,
                    "/topic/group/",
                    messageResponse,
                    UserSummary::getUserId
            );

            // 2. 오프라인 참여자에게 푸시 알림 전송
            GroupChatRoom groupChatRoom = getChatRoomById(groupChatroomId);
            GroupMessage message = groupMessageRepository.findById(messageResponse.groupMessageId())
                    .orElseThrow(() -> new BaseException(ChatResponseStatus.MESSAGE_NOT_FOUND));

            // 오프라인 참여자에게 푸시 알림 전송
            sendPushNotificationsToOfflineReceivers(
                    message,
                    groupChatRoom,
                    senderId,
                    "/topic/group",
                    GroupMessage::getMessage,                         // 메시지 내용 추출
                    GroupMessage::getUser,                            // 발신자 정보 추출
                    groupUserChatRoomRepository::findByGroupChatroom, // 참여자 목록 조회
                    GroupUserChatRoom::getUser,                       // 사용자 정보 추출
                    GroupUserChatRoom::getPushNotificationOn,         // 알림 설정 조회
                    this::isUserConnectedToWebSocket,                 // 웹소켓 연결 확인
                    (sender, recipient, content) -> FcmSendDto.builder() // 알림 객체 생성
                            .title(sender.getNickname() + "님의 그룹 메시지")
                            .body(content)
                            .token(recipient.getFcmToken())
                            .build(),
                    fcmService::sendMessage                           // 알림 전송
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

    @Override
    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // 그룹 채팅방은 검증 로직이 필요 없음 (DM과 달리 참여자 수 제한이 없음)
        // 단, 현재 사용자가 포함되어 있는지는 확인
        if (!userIds.contains(currentUserId)) {
            throw new BaseException(ChatResponseStatus.INVALID_GROUP_USER);
        }
    }
    // =====
}