package org.glue.glue_be.notification.service;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.notification.dto.request.BulkNotificationRequest;
import org.glue.glue_be.notification.dto.request.CreateNotificationRequest;
import org.glue.glue_be.notification.dto.response.NotificationResponse;
import org.glue.glue_be.notification.entity.Notification;
import org.glue.glue_be.notification.repository.NotificationRepository;
import org.glue.glue_be.notification.response.NotificationResponseStatus;
import org.glue.glue_be.user.entity.User;
import org.glue.glue_be.user.repository.UserRepository;
import org.glue.glue_be.user.response.UserResponseStatus;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.dto.MultiFcmSendDto;
import org.glue.glue_be.util.fcm.service.FcmService;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
@Transactional
public class NotificationService {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    private final FcmService fcmService;

    // 알림 작성(단일) - 백엔드 내부에서만 사용
    public void create(CreateNotificationRequest request) {
        User receiver = findUserById(request.getReceiverId());

        Notification notification = buildNotification(
                receiver,
                request.getType(),
                request.getTitle(),
                request.getContent(),
                request.getTargetId(),
                request.getGuestbookHostId()
        );

        notificationRepository.save(notification);

        // fcm을 이용해서 사용자 기기에 알림 전송
        fcmService.sendMessage(FcmSendDto.builder()
                .title(notification.getTitle())
                .body(notification.getContent())
                .token(receiver.getFcmToken())
                .build());
    }

    // 알림 작성(다수) - 백엔드 내부에서만 사용
    public void createBulk(BulkNotificationRequest request) {
        List<String> tokens = new ArrayList<>();

        for (Long receiverId : request.getReceiverIds()) {
            User receiver = userRepository.findById(receiverId)
                    .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));

            Notification notification = buildNotification(
                    receiver,
                    request.getType(),
                    request.getTitle(),
                    request.getContent(),
                    request.getTargetId(),
                    request.getGuestbookHostId()
            );

            notificationRepository.save(notification);
            tokens.add(receiver.getFcmToken());
        }

        fcmService.sendMultiMessage(MultiFcmSendDto.builder()
                .title(request.getTitle())
                .body(request.getContent())
                .tokens(tokens)
                .build());
    }


    // 알림 삭제 - 본인 알림만
    public void delete(Long notificationId, Long currentUserId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(() -> new BaseException(
                NotificationResponseStatus.NOTIFICATION_NOT_FOUND));

        if (!notification.getReceiver().getUserId().equals(currentUserId)) {
            throw new BaseException(NotificationResponseStatus.NOT_OWNER_OF_NOTIFICATION);
        }

        notificationRepository.delete(notification);
    }

    // 전체 읽기
    @Transactional(readOnly = true)
    public NotificationResponse[] getNotifications(Long currentUserId, Long cursorId, Integer pageSize,
                                                   boolean isNotice) {
        Pageable pageable = PageRequest.of(0, pageSize + 1);
        List<Notification> notifications;

        if (isNotice) {
            // notice만 조회
            notifications = (cursorId == null)
                    ? notificationRepository.findByReceiver_UserIdAndTypeOrderByNotificationIdDesc(currentUserId,
                    "notice", pageable)
                    : notificationRepository.findByReceiver_UserIdAndTypeAndNotificationIdLessThanOrderByNotificationIdDesc(
                            currentUserId, "notice", cursorId, pageable);
        } else {
            // guestbook + post 조회
            notifications = (cursorId == null)
                    ? notificationRepository.findByReceiver_UserIdAndTypeInOrderByNotificationIdDesc(currentUserId,
                    List.of("guestbook", "post"), pageable)
                    : notificationRepository.findByReceiver_UserIdAndTypeInAndNotificationIdLessThanOrderByNotificationIdDesc(
                            currentUserId, List.of("guestbook", "post"), cursorId, pageable);
        }

        boolean hasNext = notifications.size() > pageSize;
        if (hasNext) {
            notifications = notifications.subList(0, pageSize);
        }

        return notifications.stream()
                .map(n -> NotificationResponse.builder()
                        .notificationId(n.getNotificationId())
                        .type(n.getType())
                        .title(n.getTitle())
                        .content(n.getContent())
                        .targetId(n.getTargetId())
                        .hostId(n.getGuestbookHost() != null ? n.getGuestbookHost().getUserId() : null)
                        .createdAt(n.getCreatedAt())
                        .build())
                .toArray(NotificationResponse[]::new);
    }


    private User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BaseException(UserResponseStatus.USER_NOT_FOUND));
    }

    private Notification buildNotification(User receiver, String type, String title, String content, Long targetId,
                                           Long guestbookHostId) {
        Notification.NotificationBuilder builder = Notification.builder()
                .receiver(receiver)
                .type(type)
                .title(title)
                .content(content)
                .targetId(targetId);

        if (guestbookHostId != null) {
            User guestbookHost = findUserById(guestbookHostId);
            builder.guestbookHost(guestbookHost);
        }

        return builder.build();
    }

}
