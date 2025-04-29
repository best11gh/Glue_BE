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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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

    // WebSocket 메시징을 처리하기 위한 도구
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다."));
    }

    // Dm 채팅방 생성
    @Transactional
    public DmChatRoomCreateResult createDmChatRoom(DmChatRoomCreateRequest request) {
        // 사용자가 정확히 2명인지 확인
        if (request.getUserIds().size() != 2) {
            throw new ChatException("Dm 채팅방은 정확히 2명의 사용자가 필요합니다.");
        }

        // 어떤 미팅에서 파생된 쪽지인지
        Long meetingId = request.getMeetingId();

        // 쪽지 참여자 두 명
        Long userId1 = request.getUserIds().get(0);
        Long userId2 = request.getUserIds().get(1);

        // (미팅, 참여자1, 참여자2) 묶음을 검증하여 이미 존재하는 채팅방이 있는지 확인
        Optional<DmChatRoom> existingChatRoom = dmChatRoomRepository.findDirectChatRoomByUserIds(meetingId, userId1, userId2);
        if (existingChatRoom.isPresent()) {
            // 이미 존재하는 dm 채팅방 상세 정보 반환
            return new DmChatRoomCreateResult(
                    getDmChatRoomDetail(existingChatRoom.get().getId()),
                    new DmActionResponse(200, "이미 존재하는 채팅방을 반환합니다.")
            );
        }

        // 새 dm방 생성
        DmChatRoom dmChatRoom = DmChatRoom.builder()
                .meeting(meetingRepository.findByMeetingId(meetingId))
                .build();
        dmChatRoomRepository.save(dmChatRoom);

        // 참여자 추가
        for (Long userId : request.getUserIds()) {
            User user = getUserById(userId);
            DmUserChatroom dmUserChatroom = DmUserChatroom.builder()
                    .user(user)
                    .dmChatRoom(dmChatRoom)
                    .build();

            dmChatRoom.addUserChatroom(dmUserChatroom);
            dmUserChatroomRepository.save(dmUserChatroom);
        }

        // dm 채팅방 상세 정보 반환
        return new DmChatRoomCreateResult(
                getDmChatRoomDetail(dmChatRoom.getId()),
                new DmActionResponse(201, "채팅방을 성공적으로 생성하였습니다.")
        );
    }

    // Dm 채팅방 상세 정보 조회: 알림 정보 필요없을 때
    public DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId) {
        return getDmChatRoomDetail(dmChatRoomId, Optional.empty());
    }

    // Dm 채팅방 상세 정보 조회: 알림 정보 필요할 때
    @Transactional(readOnly = true)
    public DmChatRoomDetailResponse getDmChatRoomDetail(Long dmChatRoomId, Optional<Long> userId) {
        // 채팅방 ID로 dm방 조회 (없으면 예외 발생)
        DmChatRoom dmChatRoom = dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));

        // 해당 dm방의 참여자 목록 조회
        List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(dmChatRoom);

        // 응답 생성
        return responseMapper.toChatRoomDetailResponse(dmChatRoom, participants, userId.orElse(null));
    }

    // 내가 호스트인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getHostedDmChatRooms(Long userId) {
        // userId로 내 정보 불러오기
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("사용자를 찾을 수 없습니다."));

        // 내가 호스트인 미팅 정보, dm방 정보 조회
        List<Meeting> hostedMeetings = meetingRepository.findByHost_UserId(userId);
        List<DmChatRoom> hostedChatRooms = dmChatRoomRepository.findByMeetingIn(hostedMeetings);

        // 채팅방 목록 정보 반환
        return convertToChatRoomResponses(hostedChatRooms, userId, currentUser);
    }

    // 내가 참석자인 DM 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<DmChatRoomListResponse> getParticipatedDmChatRooms(Long userId) {
        // userId로 내 정보 불러오기
        User currentUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserException("사용자를 찾을 수 없습니다."));

        // 내가 참석자인 미팅 정보, dm방 정보 조회
        List<Participant> participations = participantRepository.findByUser_UserId(userId);
        List<Meeting> participatedMeetings = participations.stream()
                .map(Participant::getMeeting)
                .collect(Collectors.toList());
        List<DmChatRoom> participatedChatRooms = dmChatRoomRepository.findByMeetingIn(participatedMeetings);

        // 채팅방 목록 정보 반환
        return convertToChatRoomResponses(participatedChatRooms, userId, currentUser);
    }

    // 채팅방 목록을 응답 객체로 변환
    private List<DmChatRoomListResponse> convertToChatRoomResponses(
            List<DmChatRoom> chatRooms, Long userId, User currentUser) {

        return chatRooms.stream()
                .map(chatRoom -> {
                    // 채팅방 참여자 조회
                    List<DmUserChatroom> participants = dmUserChatroomRepository.findByDmChatRoom(chatRoom);

                    // 상대방 정보 찾기
                    User otherUser = participants.stream()
                            .map(DmUserChatroom::getUser)
                            .filter(user -> !user.getUserId().equals(userId))
                            .findFirst()
                            .orElse(currentUser);

                    // 마지막 메시지 조회
                    DmMessage lastMessage = dmMessageRepository.findTopByDmChatRoomOrderByCreatedAtDesc(chatRoom)
                            .orElse(null);

                    // 읽지 않은 메시지 있는지 확인
                    boolean hasUnreadMessages = dmMessageRepository.existsByDmChatRoomAndUser_UserIdNotAndIsRead(
                            chatRoom, userId, 0);

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
        DmChatRoom dmChatRoom = dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));

        User user = getUserById(userId);

        // 사용자가 채팅방에 참여하고 있는지 확인
        Object userChatroom = dmUserChatroomRepository.findByDmChatRoomAndUser(dmChatRoom, user)
                .orElseThrow(() -> new ChatException("해당 채팅방에 참여하고 있지 않습니다."));

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
        DmChatRoom dmChatRoom = dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));

        List<DmMessage> messages = dmMessageRepository.findByDmChatRoomOrderByCreatedAtAsc(dmChatRoom);

        // 현재 사용자가 받은 읽지 않은 메시지 상태 업데이트
        List<DmMessage> unreadMessages = messages.stream()
                .filter(message -> !message.getUser().getUserId().equals(userId)) // 본인이 보낸 메시지가 아닌 경우만
                .filter(message -> message.getIsRead() == 0) // 읽지 않은 메시지만
                .toList();
        markMessagesAsRead(dmChatRoomId, userId);

        return messages.stream()
                .map(responseMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    // websocket: Dm 전송
    @Transactional
    public void processDmMessage(Long dmChatRoomId, DmMessageSendRequest request) {
        // 1. 메시지 저장
        DmMessageResponse message = saveDmMessage(dmChatRoomId, request);

        // 2. 메시지 전송
        sendDmMessageToParticipants(dmChatRoomId, message, request.getSenderId());
    }

    private DmMessageResponse saveDmMessage(Long dmChatRoomId, DmMessageSendRequest request) {
        DmChatRoom dmChatRoom = dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));

        User sender = getUserById(request.getSenderId());

        // 사용자가 채팅방에 참여 중인지 확인
        boolean isParticipant = dmUserChatroomRepository.findByDmChatRoomAndUser(dmChatRoom, sender).isPresent();
        if (!isParticipant) {
            throw new ChatException("채팅방에 참여하지 않은 사용자입니다.");
        }

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

    private void sendDmMessageToParticipants(Long dmChatRoomId, DmMessageResponse message, Long senderId) {
        // 채팅방의 상세 정보 조회
        DmChatRoomDetailResponse chatRoom = getDmChatRoomDetail(dmChatRoomId);

        // 발신자를 제외한 참여자들에게 메시지 전송
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
        DmChatRoom dmChatRoom = dmChatRoomRepository.findById(dmChatRoomId)
                .orElseThrow(() -> new ChatException("채팅방을 찾을 수 없습니다."));

        // 사용자 존재 확인
        User receiver = getUserById(userId);

        // 사용자가 채팅방에 참여 중인지 확인
        boolean isParticipant = dmUserChatroomRepository.findByDmChatRoomAndUser(dmChatRoom, receiver).isPresent();
        if (!isParticipant) {
            throw new ChatException("채팅방에 참여하지 않은 사용자입니다.");
        }

        // 안 읽은 메시지 조회
        List<DmMessage> unreadMessages = dmMessageRepository.findUnreadMessages(dmChatRoomId, userId);

        if (!unreadMessages.isEmpty()) {
            // 일괄 읽음 처리
            unreadMessages.forEach(message -> message.setIsRead(1));
            dmMessageRepository.saveAll(unreadMessages); // 일괄 저장으로 성능 개선

            // 송신자에게 읽음 상태 업데이트 알림
            notifyDmMessageRead(dmChatRoomId, userId, unreadMessages);
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