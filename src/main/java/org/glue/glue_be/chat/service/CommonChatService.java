package org.glue.glue_be.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.response.ActionResponse;
import org.glue.glue_be.chat.exception.ChatException;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.entity.Participant;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.stream.Collectors;

@Slf4j
public abstract class CommonChatService {

    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected MeetingRepository meetingRepository;
    @Autowired
    protected ParticipantRepository participantRepository;
    @Autowired
    protected SimpMessagingTemplate messagingTemplate;

    // === 공통 사용자 및 미팅 관련 메서드 ===

    protected User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ChatException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    protected <T> void sendWebSocketMessage(String destination, T payload) {
        messagingTemplate.convertAndSend(destination, payload);
    }

    protected Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findByMeetingId(meetingId);
    }

    protected boolean isUserHostOfMeeting(Long userId, Meeting meeting) {
        return meeting.getHost().getUserId().equals(userId);
    }

//    protected boolean isUserParticipantOfMeeting(Long userId, Long meetingId) {
//        return participantRepository.existsByUser_UserIdAndMeeting_MeetingId(userId, meetingId);
//    }

    // 채팅방 생성
    protected <REQ, C, UC, R> R createChatRoom(
            REQ request,
            Long userId,
            Function<REQ, Long> meetingIdExtractor,
            Function<REQ, List<Long>> userIdsExtractor,
            TriFunction<Long, Long, Long, Optional<C>> existingChatRoomFinder,
            BiFunction<Meeting, User, C> chatRoomCreator,
            TriFunction<C, User, User, UC> userChatroomCreator,
            BiFunction<C, Integer, R> successResponseCreator,
            BiFunction<C, Integer, R> existingResponseCreator,
            Consumer<UC> userChatroomSaver) {

        // 현재 사용자 조회
        User currentUser = getUserById(userId);

        // 미팅 ID 추출
        Long meetingId = meetingIdExtractor.apply(request);

        // 사용자 ID 목록 추출
        List<Long> userIds = userIdsExtractor.apply(request);

        // DM 채팅방의 경우 특별 검증 (추후 서브클래스에서 오버라이드 가능)
        validateChatRoomUsers(userIds, userId);

        // 미팅 조회
        Meeting meeting = getMeetingById(meetingId);

        // 기존 채팅방 검색 - 있으면 바로 반환 (DM 전용 로직)
        // 그룹 채팅에서는 null이나 비어있는 Optional을 반환하도록 구현
        Optional<C> existingChatRoom = existingChatRoomFinder.apply(meetingId, userIds.get(0), userIds.get(1));
        if (existingChatRoom.isPresent()) {
            return existingResponseCreator.apply(existingChatRoom.get(), 200);
        }

        // 새 채팅방 생성
        C chatRoom = chatRoomCreator.apply(meeting, currentUser);

        // 사용자 추가
        for (Long participantId : userIds) {
            User participant = getUserById(participantId);
            UC userChatroom = userChatroomCreator.apply(chatRoom, currentUser, participant);
            userChatroomSaver.accept(userChatroom);
        }

        // 결과 반환
        return successResponseCreator.apply(chatRoom, 201);
    }

    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // 기본 구현 없음
    }

    // === 채팅방 메시지 처리 관련 공통 메서드 ===

    protected <M, C, UC> void processMarkAsRead(
            Long chatRoomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            BiFunction<C, User, UC> memberValidator,
            BiFunction<Long, Long, List<M>> unreadMessagesFinder,
            Consumer<M> messageReader,
            Consumer<List<M>> saveUpdatedMessages,
            TriConsumer<Long, Long, List<M>> notifyMessageRead) {

        User user = getUserById(userId);
        C chatRoom = chatRoomFinder.apply(chatRoomId);
        memberValidator.apply(chatRoom, user);

        List<M> unreadMessages = unreadMessagesFinder.apply(chatRoomId, userId);

        if (!unreadMessages.isEmpty()) {
            unreadMessages.forEach(messageReader);
            saveUpdatedMessages.accept(unreadMessages);
            notifyMessageRead.accept(chatRoomId, userId, unreadMessages);
        }
    }

    // 메시지 전송
    protected <C, M, R> R saveMessage(
            Long chatRoomId,
            Long senderId,
            String content,
            Function<Long, C> chatRoomFinder,
            BiFunction<C, User, ?> memberValidator,
            TriFunction<C, User, String, M> messageCreator,
            Function<M, M> messageSaver,
            Function<M, R> responseMapper) {

        // 채팅방 정보 확인 -> 발신자 정보 확인 -> 해당 유저가 채팅방에 참여 중인지 확인
        C chatRoom = chatRoomFinder.apply(chatRoomId);
        User sender = getUserById(senderId);
        memberValidator.apply(chatRoom, sender);

        // 메시지 생성
        M message = messageCreator.apply(chatRoom, sender, content);

        // 메시지 저장
        M savedMessage = messageSaver.apply(message);

        // 응답 생성
        return responseMapper.apply(savedMessage);
    }

    /**
     * 메시지 응답에 대한 웹소켓 알림 전송
     */
    protected <M, P> void notifyParticipantsExceptSender(
            List<P> participants,
            Long senderId,
            String destination,
            M message,
            Function<P, Long> userIdExtractor) {

        for (P participant : participants) {
            Long participantId = userIdExtractor.apply(participant);
            if (!participantId.equals(senderId)) {
                messagingTemplate.convertAndSend(destination + participantId, message);
            }
        }
    }

    // 채팅방 나가기
    protected <C, UC, M> List<? extends ActionResponse> processLeaveChatRoom(
            Long chatRoomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            Function<Long, User> userFinder,
            BiFunction<C, User, UC> memberValidator,
            BiConsumer<C, User> removeMember,
            Function<C, List<UC>> getRemainingMembers,
            Function<C, List<M>> getChatMessages,
            Consumer<List<M>> deleteMessages,
            Consumer<C> deleteChatRoom,
            Function<Integer, ? extends ActionResponse> createSuccessResponse) {

        List<ActionResponse> results = new ArrayList<>();

        // 채팅방 정보 확인 -> 유저 정보 확인 -> 해당 유저가 채팅방에 참여 중인지 확인
        C chatRoom = chatRoomFinder.apply(chatRoomId);
        User user = userFinder.apply(userId);
        memberValidator.apply(chatRoom, user);

        // 사용자를 채팅방에서 제거
        removeMember.accept(chatRoom, user);
        results.add(createSuccessResponse.apply(200));

        // 남은 멤버가 없으면 채팅방 및 메시지 삭제
        List<UC> remainingMembers = getRemainingMembers.apply(chatRoom);
        if (remainingMembers.isEmpty()) {
            // 해당 채팅방의 모든 메시지를 db에서 삭제
            List<M> messages = getChatMessages.apply(chatRoom);
            deleteMessages.accept(messages);
            results.add(createSuccessResponse.apply(200));

            // 채팅방 정보를 db에서 삭제
            deleteChatRoom.accept(chatRoom);
            results.add(createSuccessResponse.apply(200));
        }

        return results;
    }

    // === 웹소켓 알림 관련 공통 메서드 ===

    /**
     * 특정 사용자에게 알림 전송
     */
    protected <T> void sendNotificationToUser(Long userId, String endpoint, T payload) {
        messagingTemplate.convertAndSend("/queue/" + endpoint + "/" + userId, payload);
    }

    /**
     * 채팅방의 모든 참여자에게 알림 전송 (발신자 제외)
     */
    protected <T, P> void notifyAllParticipantsExceptSender(
            Long chatRoomId,
            Long senderId,
            String endpoint,
            T payload,
            Function<Long, List<P>> participantsFinder,
            Function<P, Long> participantIdExtractor) {

        List<P> participants = participantsFinder.apply(chatRoomId);

        participants.stream()
                .map(participantIdExtractor)
                .filter(id -> !id.equals(senderId))
                .forEach(id -> sendNotificationToUser(id, endpoint, payload));
    }

    // === 채팅방 목록 조회 관련 공통 메서드 ===

    /**
     * 특정 미팅의 채팅방 목록 조회 템플릿 메서드
     */
    protected <C, R> List<R> getChatRoomsForMeeting(
            Long meetingId,
            Long userId,
            Function<Long, Meeting> meetingFinder,
            Function<Meeting, List<C>> chatRoomsFinder,
            BiFunction<List<C>, User, List<R>> responseConverter) {

        User currentUser = getUserById(userId);
        Meeting meeting = meetingFinder.apply(meetingId);
        List<C> chatRooms = chatRoomsFinder.apply(meeting);

        return responseConverter.apply(chatRooms, currentUser);
    }

    // 채팅방 목록 조회
    protected <C, R> List<R> getChatRooms(
            Long userId,
            Function<Long, User> userFinder,
            Function<User, List<C>> chatRoomsFinder,
            BiFunction<List<C>, User, List<R>> responseConverter) {

        // 1. 로그인한 유저 정보 조회
        User currentUser = userFinder.apply(userId);

        // 2. 채팅방 조회
        List<C> chatRooms = chatRoomsFinder.apply(currentUser);

        // 3. 응답 생성
        return responseConverter.apply(chatRooms, currentUser);
    }

    // === 채팅방 생성 관련 공통 메서드 ===

    /**
     * 채팅방 생성 공통 로직
     */
    protected <C, R, REQ> R createChatRoom(
            REQ request,
            Long userId,
            Function<REQ, Long> meetingIdExtractor,
            Function<Long, Meeting> meetingFinder,
            BiFunction<Meeting, REQ, C> chatRoomCreator,
            Function<C, R> responseCreator) {

        Long meetingId = meetingIdExtractor.apply(request);
        Meeting meeting = meetingFinder.apply(meetingId);
        C chatRoom = chatRoomCreator.apply(meeting, request);

        return responseCreator.apply(chatRoom);
    }

    // === 유틸리티 인터페이스 ===

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }

    /**
     * 에러 로깅
     */
    protected void logError(String message, Exception e) {
        log.error(message, e);
    }

    /**
     * 디버그 로깅
     */
    protected void logDebug(String message, Object... args) {
        log.debug(message, args);
    }

    /**
     * 인포 로깅
     */
    protected void logInfo(String message, Object... args) {
        log.info(message, args);
    }

    /**
     * 채팅방 정보 업데이트 템플릿 메서드
     */
    protected <C, R> R updateChatRoomInfo(
            Long chatRoomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            BiPredicate<C, Long> isAllowed,
            Consumer<C> updater,
            Function<C, R> responseCreator) {

        C chatRoom = chatRoomFinder.apply(chatRoomId);

        if (!isAllowed.test(chatRoom, userId)) {
            throw new ChatException("해당 작업을 수행할 권한이 없습니다.");
        }

        updater.accept(chatRoom);
        return responseCreator.apply(chatRoom);
    }
}