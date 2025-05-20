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

    // 채팅방 상세 보기에서 방장인지 아닌지 구분하기 위한 매소드
    protected <UC, C, M> boolean determineIsHost(
            UC userChatroom,
            Function<UC, User> userExtractor,
            Function<UC, C> chatroomExtractor,
            Function<C, M> meetingExtractor,
            Function<M, User> hostExtractor) {
        try {
            // 사용자와 채팅방 정보 가져오기
            User user = userExtractor.apply(userChatroom);
            C chatroom = chatroomExtractor.apply(userChatroom);

            if (user == null || chatroom == null) {
                return false;
            }

            // 채팅방에서 meeting 객체 추출
            M meeting = meetingExtractor.apply(chatroom);

            if (meeting == null) {
                return false; // 미팅 정보가 없으면 호스트가 아님
            }

            // 미팅에서 host 추출
            User host = hostExtractor.apply(meeting);

            if (host == null) {
                return false; // 호스트 정보가 없으면 호스트가 아님
            }

            // 사용자 ID와 호스트 ID가 같은지 확인
            return user.getUserId().equals(host.getUserId());

        } catch (Exception e) {
            return false; // 예외 발생 시 기본적으로 호스트가 아님으로 처리
        }
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

    // 채팅방 알림 토글
    protected <C, UC> Integer processTogglePushNotification(
            Long chatroomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            Function<Long, User> userFinder,
            BiFunction<C, User, UC> memberValidator,
            Function<UC, Integer> notificationStatusGetter,
            BiConsumer<UC, Integer> notificationStatusSetter,
            Function<UC, UC> userChatroomSaver
    ) {
        // 채팅방 및 사용자 조회
        C chatRoom = chatRoomFinder.apply(chatroomId);
        User user = userFinder.apply(userId);

        // 사용자가 채팅방 멤버인지 확인
        UC userChatroom = memberValidator.apply(chatRoom, user);

        // 현재 알림 상태 확인
        Integer currentStatus = notificationStatusGetter.apply(userChatroom);

        // 알림 상태 토글 (1→0, 0→1)
        Integer newStatus = (currentStatus == 1) ? 0 : 1;

        // 업데이트된 알림 상태 설정
        notificationStatusSetter.accept(userChatroom, newStatus);

        userChatroomSaver.apply(userChatroom);

        return newStatus;
    }

    // 채팅방 나가기
    // 채팅방 나가기
    protected <C, UC, M> List<ActionResponse> processLeaveChatRoom(
            Long chatRoomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            Function<Long, User> userFinder,
            BiFunction<C, User, UC> memberValidator,
            BiConsumer<C, User> removeMember,
            Function<C, List<UC>> getRemainingMembers,
            Function<C, List<M>> getChatMessages,
            Consumer<List<M>> deleteMessages,
            Consumer<C> deleteChatRoom) {

        List<ActionResponse> results = new ArrayList<>();

        // 채팅방 정보 확인 -> 유저 정보 확인 -> 해당 유저가 채팅방에 참여 중인지 확인
        C chatRoom = chatRoomFinder.apply(chatRoomId);
        User user = userFinder.apply(userId);
        memberValidator.apply(chatRoom, user);

        // 사용자를 해당 채팅방 db에서 제거
        removeMember.accept(chatRoom, user);
        results.add(new ActionResponse(200, getLeaveStatusMessage(200)));  // 직접 ActionResponse 생성

        // 남은 멤버가 없으면 채팅방 및 메시지 삭제
        List<UC> remainingMembers = getRemainingMembers.apply(chatRoom);
        if (remainingMembers.isEmpty()) {
            // 해당 채팅방의 모든 메시지를 db에서 삭제
            List<M> messages = getChatMessages.apply(chatRoom);
            deleteMessages.accept(messages);
            results.add(new ActionResponse(201, getLeaveStatusMessage(201)));  // 201 상태코드 사용

            // 채팅방 정보를 db에서 삭제
            deleteChatRoom.accept(chatRoom);
            results.add(new ActionResponse(202, getLeaveStatusMessage(202)));  // 202 상태코드 사용
        }

        return results;
    }

    // 채팅방 나가기 관련 응답 코드
    private String getLeaveStatusMessage(int status) {
        switch (status) {
            case 200: return "채팅방에서 성공적으로 퇴장하였습니다.";
            case 201: return "채팅방의 모든 메시지를 성공적으로 삭제하였습니다.";
            case 202: return "채팅방을 성공적으로 삭제하였습니다.";
            default: return "작업이 완료되었습니다.";
        }
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
    public boolean isUserConnectedToWebSocket(Long userId, String deliveryType) {
        // 사용자의 구독 주소 확인
        String destination = deliveryType + "/" + userId;

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
    public <C, M, UC, NT> void sendPushNotificationsToOfflineReceivers(
            M message,
            C chatRoom,
            Long senderId,
            String deliveryType,
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
            if (isUserConnected.test(recipientId, deliveryType)) {
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

    protected User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException.UserNotFoundException(userId));
    }

    protected Meeting getMeetingById(Long meetingId) {
        return meetingRepository.findByMeetingId(meetingId);
    }

    protected void validateChatRoomUsers(List<Long> userIds, Long currentUserId) {
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