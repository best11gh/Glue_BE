package org.glue.glue_be.chat.service;

import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.chat.dto.response.ActionResponse;
import org.glue.glue_be.chat.dto.response.ChatResponseStatus;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.meeting.entity.Meeting;
import org.glue.glue_be.meeting.repository.MeetingRepository;
import org.glue.glue_be.meeting.repository.ParticipantRepository;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.exception.UserException;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.service.FcmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.user.SimpSession;
import org.springframework.messaging.simp.user.SimpSubscription;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.ArrayList;
import java.util.List;
import java.util.function.*;
import org.springframework.data.domain.Pageable;

@Slf4j
public abstract class CommonChatService {

    @Autowired protected UserRepository userRepository;
    @Autowired protected MeetingRepository meetingRepository;
    @Autowired protected ParticipantRepository participantRepository;
    @Autowired protected SimpMessagingTemplate messagingTemplate;
    @Autowired private SimpUserRegistry simpUserRegistry;
    @Autowired private FcmService fcmService;

    // 채팅방 상세 보기에서 방장인지 아닌지 구분하기 위한 매소드(완장 표시 위함)
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
                return false; // 예외 발생 시 기본적으로 호스트가 아님으로 처리
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
            Long cursorId,
            Integer pageSize,
            Long userId,
            Function<Long, User> userFinder,
            BiFunction<User, Pageable, List<C>> initialChatRoomsFinder,    // 첫 페이지 조회
            TriFunction<User, Long, Pageable, List<C>> cursorChatRoomsFinder, // 커서 기반 조회
            BiFunction<List<C>, User, List<R>> responseConverter) {

        // 1. 페이징 설정 (pageSize + 1건으로 hasNext 판단용)
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 로그인한 유저 정보 조회
        User currentUser = userFinder.apply(userId);

        // 2. 커서 기반 채팅방 조회
        List<C> chatRooms = (cursorId == null)
                ? initialChatRoomsFinder.apply(currentUser, pageable)
                : cursorChatRoomsFinder.apply(currentUser, cursorId, pageable);

        // 3. 다음 페이지 존재 확인
        boolean hasNext = chatRooms.size() > pageSize;
        if (hasNext) {
            chatRooms = chatRooms.subList(0, pageSize);  // 실제 데이터는 pageSize만큼만
        }

        // 4. DTO 반환
        List<R> responses = responseConverter.apply(chatRooms, currentUser);

        return responses;
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
        results.add(new ActionResponse(ChatResponseStatus.CHATROOM_LEFT.getCode(), ChatResponseStatus.CHATROOM_LEFT.getMessage()));

        // 남은 멤버가 없으면 채팅방 및 메시지 삭제
        List<UC> remainingMembers = getRemainingMembers.apply(chatRoom);
        if (remainingMembers.isEmpty()) {
            // 해당 채팅방의 모든 메시지를 db에서 삭제
            List<M> messages = getChatMessages.apply(chatRoom);
            deleteMessages.accept(messages);
            results.add(new ActionResponse(201, "채팅방의 모든 메시지를 성공적으로 삭제하였습니다."));

            // 채팅방 정보를 db에서 삭제
            deleteChatRoom.accept(chatRoom);
            results.add(new ActionResponse(202, "채팅방을 성공적으로 삭제하였습니다."));
        }

        return results;
    }

    // 무한 스크롤로 메시지 조회
    public <T, R, C> List<R> getMessagesWithPagination(
            Long cursorId,
            Integer pageSize,
            C chatRoom,
            BiFunction<C, Pageable, List<T>> initialMessagesFinder,
            TriFunction<C, Long, Pageable, List<T>> cursorMessagesFinder,
            Function<T, R> responseMapper,
            Runnable readMarker) {

        // 페이징 설정 (pageSize + 1건으로 hasNext 판단용)
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        // 커서 기반 메시지 조회 (ID 내림차순으로 조회)
        List<T> messages = (cursorId == null)
                ? initialMessagesFinder.apply(chatRoom, pageable)
                : cursorMessagesFinder.apply(chatRoom, cursorId, pageable);

        // 다음 페이지 존재 확인 후 실제 데이터만 반환
        if (messages.size() > pageSize) {
            messages = messages.subList(0, pageSize);
        }

        // 읽지 않은 메시지 읽음 처리
        readMarker.run();

        // 시간순 정렬 (오래된 메시지 → 최신 메시지)
        Collections.reverse(messages);

        // 응답 객체로 변환
        return messages.stream()
                .map(responseMapper)
                .collect(Collectors.toList());
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

        try {
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
        } catch (Exception e) {
            throw new BaseException(ChatResponseStatus.MESSAGE_SENDING_FAILED);
        }
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

    // 읽음 처리
    protected <C, UC> void processMarkAsRead(
            Long chatRoomId,
            Long userId,
            Function<Long, C> chatRoomFinder,
            BiFunction<C, User, UC> memberValidator,
            Function<C, Long> latestMessageIdFinder,
            TriConsumer<Long, Long, Long> lastReadMessageUpdater) {

        User user = getUserById(userId);
        C chatRoom = chatRoomFinder.apply(chatRoomId);
        memberValidator.apply(chatRoom, user);

        // 해당 채팅방의 가장 최신 메시지 ID 조회
        Long latestMessageId = latestMessageIdFinder.apply(chatRoom);

        if (latestMessageId != null && latestMessageId > 0) {
            // 마지막 읽은 메시지 ID 업데이트
            lastReadMessageUpdater.accept(userId, chatRoomId, latestMessageId);
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

    protected <M, C, UC> void sendPushNotificationToUser(
            M message,
            Long recipientId,
            C chatRoom,
            Function<C, Long> chatRoomIdExtractor,              // 추가: 채팅방 ID 추출
            Function<M, String> contentExtractor,
            Function<M, User> senderExtractor,
            BiFunction<Long, Long, Optional<UC>> userChatRoomFinder, // userId, chatRoomId -> UserChatRoom
            Function<UC, Integer> notificationSettingGetter,
            String notificationTitle) {

        // 채팅방 ID 추출
        Long chatRoomId = chatRoomIdExtractor.apply(chatRoom);

        // 알림 설정 확인
        Optional<UC> userChatRoom = userChatRoomFinder.apply(recipientId, chatRoomId);

        if (userChatRoom.isEmpty() || notificationSettingGetter.apply(userChatRoom.get()) != 1) {
            return; // 알림 설정이 꺼져있음
        }

        User recipient = getUserById(recipientId);
        if (recipient.getFcmToken() == null) {
            return; // FCM 토큰이 없음
        }

        String content = contentExtractor.apply(message);
        if (content.length() > 100) {
            content = content.substring(0, 97) + "...";
        }

        User sender = senderExtractor.apply(message);

        FcmSendDto fcmDto = FcmSendDto.builder()
                .title(sender.getNickname() + notificationTitle)
                .body(content)
                .token(recipient.getFcmToken())
                .build();

        fcmService.sendMessage(fcmDto);
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