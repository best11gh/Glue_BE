package org.glue.glue_be.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.response.ActionResponse;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.exception.UserException;
import org.glue.glue_be.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;
import java.util.Map;
import java.util.HashMap;

@Slf4j
public abstract class CommonChatService {

    @Autowired protected UserRepository userRepository;
    @Autowired protected MeetingRepository meetingRepository;
    @Autowired protected ParticipantRepository participantRepository;
    @Autowired protected SimpMessagingTemplate messagingTemplate;
    @Autowired private SimpUserRegistry simpUserRegistry;

    protected User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFoundException(userId));
    }

    protected Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findByMeetingId(meetingId);
    }

    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
        // 기본 구현 없음
    }

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

        // 현재 사용자 조회, 미팅 ID 추출, 사용자 ID 목록 추출
        User currentUser = getUserById(userId);
        Long meetingId = meetingIdExtractor.apply(request);
        List<Long> userIds = userIdsExtractor.apply(request);

        // DM 채팅방의 경우 특별 검증 (서브클래스에서 오버라이드)
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

    // 채팅방 목록 조회
    protected <C, R> List<R> getChatRooms(
            Long userId,
            Function<Long, User> userFinder,
            Function<User, List<C>> chatRoomsFinder,
            BiFunction<List<C>, User, List<R>> responseConverter) {

        // 로그인한 유저 정보, 채팅방 정보 조회
        User currentUser = userFinder.apply(userId);
        List<C> chatRooms = chatRoomsFinder.apply(currentUser);

        // 응답 생성
        return responseConverter.apply(chatRooms, currentUser);
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

        // 사용자를 해당 채팅방 db에서 제거
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

    // 메시지 db에 저장
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

    // 사용자의 웹소켓 연결 상태를 확인
    protected boolean isUserConnectedToWebSocket(Long userId, String chatType) {
        // 사용자의 구독 주소 확인
        String destination = "/queue/" + chatType + "/" + userId;

        // 모든 사용자 순회
        for (SimpUser user : simpUserRegistry.getUsers()) {
            // 각 사용자의 모든 세션 순회
            for (SimpSession session : user.getSessions()) {
                // 해당 세션의 모든 구독 확인
                for (SimpSubscription subscription : session.getSubscriptions()) {
                    // 구독 주소가 일치하면 연결된 것으로 판단
                    if (subscription.getDestination().equals(destination)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    // 온라인 상태인 유저들에게 웹소켓 알림 전송
    // convertAndSend() 매소드가 연결 상태를 자체적으로 확인한다!
    protected <M, P> void sendWebSocketMessageToOnlineReceivers(
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

    // 메시지 읽음 처리
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

    // 알림 전송
    protected <T> void sendNotificationToUser(Long userId, String endpoint, T payload) {
        messagingTemplate.convertAndSend("/queue/" + endpoint + "/" + userId, payload);
    }

    // FCM 알림 전송
    protected <C, M, UC, NT> void sendPushNotificationsToOfflineReceivers(
            M message,
            C chatRoom,
            Long senderId,
            Function<M, String> contentExtractor,
            Function<M, User> senderExtractor,
            Function<C, List<UC>> participantsGetter,
            Function<UC, User> userExtractor,
            Function<UC, Integer> notificationSettingGetter,
            BiPredicate<Long, String> isUserConnected,
            TriFunction<User, User, String, NT> notificationBuilder,
            Consumer<NT> notificationSender) {

        // 메시지 발신자 정보
        User sender = senderExtractor.apply(message);

        // 메시지 내용
        String content = contentExtractor.apply(message);
        // 내용이 너무 길 경우 잘라내기
        if (content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }

        // 채팅방 참여자 목록 조회
        List<UC> participants = participantsGetter.apply(chatRoom);

        // 채팅방 유형 (일반적으로 구현 클래스에 의해 결정됨)
        String chatType = getChatType(chatRoom);

        // 발신자를 제외한 모든 참여자에게 알림 전송 시도
        for (UC participant : participants) {
            User recipientUser = userExtractor.apply(participant);
            Long recipientId = recipientUser.getUserId();

            // 발신자에게는 알림을 보내지 않음
            if (recipientId.equals(senderId)) {
                continue;
            }

            // 알림 설정 확인 (1인 경우에만 알림 전송)
            Integer notificationSetting = notificationSettingGetter.apply(participant);
            if (notificationSetting != 1) {
                continue;
            }

            // 웹소켓 연결 상태 확인 (연결되어 있지 않은 경우에만 알림 전송)
            if (isUserConnected.test(recipientId, chatType)) {
                continue;
            }

            // 알림 객체 생성
            NT notification = notificationBuilder.apply(
                    sender,
                    recipientUser,
                    content
            );

            // 알림 전송
            notificationSender.accept(notification);
        }
    }

    // 채팅방 ID 추출 메서드 (구현 클래스에서 오버라이드)
    protected <C> Long getChatRoomId(C chatRoom) {
        throw new UnsupportedOperationException("구현 클래스에서 오버라이드해야 합니다.");
    }

    // 채팅 유형 반환 메서드 (구현 클래스에서 오버라이드)
    protected <C> String getChatType(C chatRoom) {
        throw new UnsupportedOperationException("구현 클래스에서 오버라이드해야 합니다.");
    }

    @FunctionalInterface
    public interface TriFunction<T, U, V, R> {
        R apply(T t, U u, V v);
    }

    @FunctionalInterface
    public interface TriConsumer<T, U, V> {
        void accept(T t, U u, V v);
    }
}