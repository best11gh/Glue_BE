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
import org.glue.glue_be.invitation.repository.InvitationRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.exception.UserException;
import org.glue.glue_be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.glue.glue_be.chat.mapper.DmResponseMapper;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DmChatService {

    private final DmChatRoomRepository dmChatRoomRepository;
    private final DmUserChatroomRepository dmUserChatroomRepository;
    private final DmMessageRepository dmMessageRepository;
    private final UserRepository userRepository;
    private final MeetingRepository meetingRepository;
    private final ParticipantRepository participantRepository;
    private final DmResponseMapper responseMapper;
    private final InvitationRepository invitationRepository;

    // WebSocket 메시징을 처리하기 위한 도구
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다."));
    }

    private DmChatRoom getChatRoomById(Long dmChatRoomId) {
        return dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));
    }

    private DmUserChatroom validateChatRoomMember(DmChatRoom chatRoom, User user) {
        return dmUserChatroomRepository.findByDmChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new ChatException("채팅방에 참여하지 않은 사용자입니다."));
    }

    // Dm 채팅방 생성
    @Transactional
    public DmChatRoomCreateResult createDmChatRoom(DmChatRoomCreateRequest request, Long userId) {
        // 현재 사용자 조회 및 검증
        User currentUser = getUserById(userId);

        // 2명 참여 확인 및 현재 사용자 포함 확인
        List<Long> userIds = request.getUserIds();
        if (userIds.size() != 2 || !userIds.contains(userId)) {
            throw new ChatException("DM 채팅방은 본인을 포함한 정확히 2명의 사용자가 필요합니다.");
        }

        Long meetingId = request.getMeetingId();

        // 기존 채팅방 검색 - 있으면 바로 반환
        Optional<DmChatRoom> existingChatRoom = dmChatRoomRepository.findDirectChatRoomByUserIds(
                meetingId, userIds.get(0), userIds.get(1));
        if (existingChatRoom.isPresent()) {
            return new DmChatRoomCreateResult(
                    getDmChatRoomDetail(existingChatRoom.get().getId()),
                    new DmActionResponse(200, "이미 존재하는 채팅방을 반환합니다.")
            );
        }

        // 새 채팅방 생성 및 사용자 연결
        Meeting meeting = meetingRepository.findByMeetingId(meetingId);
        DmChatRoom chatRoom = dmChatRoomRepository.save(
                DmChatRoom.builder().meeting(meeting).build()
        );

        // 사용자 추가
        userIds.forEach(participantId -> {
            User user = getUserById(participantId);
            DmUserChatroom userChatroom = DmUserChatroom.builder()
                    .user(user)
                    .dmChatRoom(chatRoom)
                    .build();

            chatRoom.addUserChatroom(userChatroom);
            dmUserChatroomRepository.save(userChatroom);
        });

        // 결과 반환
        return new DmChatRoomCreateResult(
                getDmChatRoomDetail(chatRoom.getId()),
                new DmActionResponse(201, "채팅방을 성공적으로 생성하였습니다.")
        );
    }

    // Dm 채팅방 상세 정보 조회: 알림 정보 필요없을 때
    private DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId) {
        return getDmChatRoomDetail(dmChatRoomId, Optional.empty());
    }

    // Dm 채팅방 상세 정보 조회: 알림 정보 필요할 때
    @Transactional(readOnly = true)
    public DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId, Optional<Long> userId) {
        // 채팅방 ID로 dm방 조회
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);

        // 해당 dm방의 참여자 목록 조회
        List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(dmChatRoom);

        // 초대장 로직 - 사용자가 로그인한 경우에만 초대 정보 확인
        // invitationStatus = -1: "모임 초대하기"가 아예 필요 없는 상황
        Integer invitationStatus = -1;
        if (userId.isPresent()) {
            invitationStatus = checkInvitationStatus(dmChatRoom, participants, userId.get());
        }

        // 응답 생성
        return responseMapper.toChatRoomDetailResponse(dmChatRoom, participants, userId.orElse(null), invitationStatus);
    }

    private Integer checkInvitationStatus(DmChatRoom dmChatRoom, List<DmUserChatroom> participants, Long userId) {
        // 1. 모임 id 추출
        Long meetingId = dmChatRoom.getMeeting().getMeetingId();

        // 2. 로그인한 사용자 == 모임의 호스트인지 확인
        boolean isHost = dmChatRoom.getMeeting().getHost().getUserId().equals(userId);

        // 2-1. !isHost일 경우 초대 상태를 확인할 필요 없음
        if (!isHost) {
            return -1;
        }

        // 3. 참여자 중 로그인한 사용자 빼고 추출
        Optional<Long> otherParticipantUserId = participants.stream()
                .map(duc -> duc.getUser().getUserId())
                .filter(id -> !id.equals(userId))
                .findFirst();

        // 3-1. 다른 참여자가 없으면 초대 상태를 확인할 필요 없음
        if (otherParticipantUserId.isEmpty()) {
            return -1;
        }

        // 4. 초대 상태 직접 조회
        return invitationRepository.findStatusByMeetingAndParticipantIds(
                meetingId, userId, otherParticipantUserId.get());
    }

    // 내가 호스트인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getHostedDmChatRooms(Long userId) {
        // 내 정보 불러오기
        User currentUser = getUserById(userId);

        // 내가 호스트인 미팅 정보, dm방 정보 조회
        List<Meeting> hostedMeetings = meetingRepository.findByHost_UserId(currentUser.getUserId());
        List<DmChatRoom> hostedChatRooms = dmChatRoomRepository.findByMeetingIn(hostedMeetings);

        // 채팅방 목록 정보 반환
        return convertToChatRoomResponses(hostedChatRooms, currentUser);
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getParticipatedDmChatRooms(Long userId) {
        // userId로 내 정보 불러오기
        User currentUser = getUserById(userId);

        // 내가 참석자인 미팅 정보, dm방 정보 조회
        List<Participant> participations = participantRepository.findByUser_UserId(currentUser.getUserId());
        List<Meeting> participatedMeetings = participations.stream()
                .map(Participant::getMeeting)
                .collect(Collectors.toList());
        List<DmChatRoom> participatedChatRooms = dmChatRoomRepository.findByMeetingIn(participatedMeetings);

        // 채팅방 목록 정보 반환
        return convertToChatRoomResponses(participatedChatRooms, currentUser);
    }

    // 채팅방 목록을 응답 객체로 변환
    private List<DmChatRoomListResponse> convertToChatRoomResponses(
            List<DmChatRoom> chatRooms, User currentUser) {

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 채팅방 참여자 조회
                    List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(chatRoom);

                    // 상대방 정보 찾기
                    User otherUser = participants.stream()
                            .map(DmUserChatroom::getUser)
                            .filter(user -> !user.getUserId().equals(currentUser.getUserId()))
                            .findFirst()
                            .orElse(currentUser);

                    // 마지막 메시지 조회
                    DmMessage lastMessage = dmMessageRepository.findTopByDmChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    // 읽지 않은 메시지 있는지 확인
                    boolean hasUnreadMessages = dmMessageRepository.existsByDmChatRoomAndUser_UserIdNotAndIsRead(
                            chatRoom, currentUser.getUserId(), 0);

                    // 응답 생성
                    return responseMapper.toChatRoomListResponse(
                            chatRoom, otherUser, lastMessage, hasUnreadMessages);
                })
                .collect(Collectors.toList());
    }

    // Dm방 나가기
    @Transactional
    public List<DmActionResponse> leaveDmChatRoom(Long dmChatRoomId, Long userId) {
        List<DmActionResponse> result = new ArrayList<>();

        // 유효한 채팅방 번호인지 확인
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);

        // 사용자 정보 불러오기
        User user = getUserById(userId);

        // 사용자가 채팅방에 참여하고 있는지 확인
        Object userChatroom = validateChatRoomMember(dmChatRoom, user);

        // 사용자를 채팅방에서 삭제
        dmUserChatroomRepository.deleteByDmChatRoomAndUser(dmChatRoom, user);
        result.add(new DmActionResponse(200, "채팅방에서 성공적으로 퇴장하였습니다."));

        // 채팅방에 남은 참여자가 없으면: 모든 메세지와 채팅방 자체를 삭제
        List<DmUserChatroom> remainingParticipants = dmUserChatroomRepository.findByDmChatRoom(dmChatRoom);
        if (remainingParticipants.isEmpty()) {
            // 메시지 먼저 삭제
            List<DmMessage> messages = dmMessageRepository.findByDmChatRoomOrderByCreatedAtAsc(dmChatRoom);
            dmMessageRepository.deleteAll(messages);
            result.add(new DmActionResponse(200, "채팅방의 모든 메시지를 성공적으로 삭제하였습니다."));

            // 채팅방 삭제
            dmChatRoomRepository.delete(dmChatRoom);
            result.add(new DmActionResponse(200, "채팅방을 성공적으로 삭제하였습니다."));
        }

        return result;
    }

    // Dm방 클릭 시, 대화 이력을 불러오면서 + 읽지 않은 메시지들 읽음으로 처리
    @Transactional
    public List<DmMessageResponse> getDmMessagesByDmChatRoomId(Long dmChatRoomId, Long userId) {
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);
        User user = getUserById(userId);

        List<DmMessage> messages = dmMessageRepository.findByDmChatRoomOrderByCreatedAtAsc(dmChatRoom);

        // 현재 사용자가 받은 읽지 않은 메시지 상태 업데이트
        List<DmMessage> unreadMessages = messages.stream()
                .filter(message -> !message.getUser().getUserId().equals(userId)) // 본인이 보낸 메시지가 아닌 경우만
                .filter(message -> message.getIsRead() == 0) // 읽지 않은 메시지만
                .toList();
        markMessagesAsRead(dmChatRoomId, user.getUserId());

        return messages.stream()
                .map(responseMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    // websocket: Dm 전송
    @Transactional
    public DmMessageResponse processDmMessage(Long dmChatRoomId, DmMessageSendRequest request, Long userId) {
        // 1. 메시지 저장
        DmMessageResponse response = saveDmMessage(dmChatRoomId, request, userId);

        // 2. 웹소켓 알림 전송
        notifyIfOnline(dmChatRoomId, response, request.getSenderId());

        return response;
    }

    private DmMessageResponse saveDmMessage(Long dmChatRoomId, DmMessageSendRequest request, Long userId) {
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);

        User sender = getUserById(request.getSenderId());

        // 사용자가 채팅방에 참여 중인지 확인
        validateChatRoomMember(dmChatRoom, sender);

        // 메시지 저장
        DmMessage dmMessage = DmMessage.builder()
                .chatRoom(dmChatRoom)
                .user(sender)
                .dmMessageContent(request.getContent())
                .build();

        dmMessageRepository.save(dmMessage);

        // 응답 생성
        return responseMapper.toMessageResponse(dmMessage);
    }

    private void notifyIfOnline(Long dmChatRoomId, DmMessageResponse message, Long senderId) {
        // 채팅방의 상세 정보 조회
        DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(dmChatRoomId);

        // 발신자를 제외한 참여자들 중 온라인 상태인 사용자에게만 WebSocket 메시지 전송
        for (UserSummary participant : chatRoom.getParticipants()) {
            if (!participant.getUserId().equals(senderId)) {
                messagingTemplate.convertAndSend("/queue/dm/" + participant.getUserId(), message);
            }
        }
    }

    // websocket: 실시간 읽음 처리
    @Transactional
    public void markMessagesAsRead(Long dmChatRoomId, Long userId) {
        // 채팅방 존재 확인
        DmChatRoom dmChatRoom = getChatRoomById(dmChatRoomId);

        // 사용자 존재 확인
        User receiver = getUserById(userId);

        // 사용자가 채팅방에 참여 중인지 확인
        validateChatRoomMember(dmChatRoom, receiver);

        // 안 읽은 메시지 조회
        List<DmMessage> unreadMessages = dmMessageRepository.findUnreadMessages(dmChatRoomId, userId);

        if (!unreadMessages.isEmpty()) {
            // 일괄 읽음 처리
            unreadMessages.forEach(message -> message.setIsRead(1));
            dmMessageRepository.saveAll(unreadMessages); // 일괄 저장으로 성능 개선

            // 송신자에게 읽음 상태 업데이트 알림
            notifyDmMessageRead(dmChatRoomId, receiver.getUserId(), unreadMessages);
        }
    }

    @Transactional
    public void notifyDmMessageRead(Long dmChatRoomId, Long receiverId, List<DmMessage> readMessages) {
        // WebSocket을 통해 실시간으로 메시지 읽음 상태 전달
        if (!readMessages.isEmpty()) {
            DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(dmChatRoomId);

            // 메시지 엔티티를 응답 DTO로 변환
            List<DmMessageResponse> updatedMessages = readMessages.stream()
                    .map(responseMapper::toMessageResponse)
                    .collect(Collectors.toList());

            // 메시지 발신자에게 WebSocket을 통해 읽음 상태 변경 정보 전송
            // 이는 발신자의 UI에서 메시지 읽음 표시를 업데이트하기 위함
            for (UserSummary participant : chatRoom.getParticipants()) {
                if (!participant.getUserId().equals(receiverId)) {
                    DmReadStatusUpdateResponse readStatus = new DmReadStatusUpdateResponse(
                            dmChatRoomId, receiverId, updatedMessages
                    );
                    // WebSocket 채널을 통해 발신자에게 읽음 상태 전송 (/queue/dm/read/{userId})
                    messagingTemplate.convertAndSend("/queue/dm/read/" + participant.getUserId(), readStatus);
                }
            }
        }
    }
}