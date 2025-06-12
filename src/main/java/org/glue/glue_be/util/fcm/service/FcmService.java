package org.glue.glue_be.util.fcm.service;

import com.google.firebase.messaging.*;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.util.fcm.dto.*;
import org.glue.glue_be.util.fcm.response.FcmResponseStatus;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FcmService {

    public void sendMessage(FcmSendDto fcmSendDto) {
        if (fcmSendDto.getToken() == null || fcmSendDto.getToken().isBlank()) {
            log.error("[FCM 단일 전송 실패] 유효하지 않은 토큰: null 또는 빈 값");
            throw new BaseException(FcmResponseStatus.FCM_SEND_ERROR, "FCM 토큰이 비어있습니다.");
        }

        if (fcmSendDto.getType() == null || fcmSendDto.getType().isBlank()) {
            log.error("[FCM 단일 전송 실패] 알림 타입이 누락되었습니다.");
            throw new BaseException(FcmResponseStatus.FCM_SEND_ERROR, "FCM 알림 type은 필수입니다.");
        }

        if (fcmSendDto.getId() == null) {
            log.error("[FCM 단일 전송 실패] 알림 대상 ID가 누락되었습니다.");
            throw new BaseException(FcmResponseStatus.FCM_SEND_ERROR, "FCM 알림 id는 필수입니다.");
        }

        Map<String, String> data = new HashMap<>();
        data.put("type", fcmSendDto.getType());
        data.put("id", fcmSendDto.getId().toString());

        if (fcmSendDto.getGuestbookHostId() != null) {
            data.put("guestbookHostId", fcmSendDto.getGuestbookHostId().toString());
        }

        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(fcmSendDto.getTitle())
                        .setBody(fcmSendDto.getBody())
                        .build())
                .putAllData(data)
                .setToken(fcmSendDto.getToken())
                .build();

        try {
            FirebaseMessaging.getInstance().send(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM 단일 전송 실패] token: {}, title: {}, body: {}, message: {}",
                    fcmSendDto.getToken(),
                    fcmSendDto.getTitle(),
                    fcmSendDto.getBody(),
                    e.getMessage(),
                    e
            );
            throw new BaseException(FcmResponseStatus.FCM_SEND_ERROR, "유효하지 않은 FCM 토큰입니다.");
        }
    }

    public void sendMultiMessage(MultiFcmSendDto multiFcmSendDto) {
        int successCount = 0;
        int failureCount = 0;

        for (String token : multiFcmSendDto.getTokens()) {
            FcmSendDto singleDto = FcmSendDto.builder()
                    .token(token)
                    .title(multiFcmSendDto.getTitle())
                    .body(multiFcmSendDto.getBody())
                    .type(multiFcmSendDto.getType())
                    .id(multiFcmSendDto.getId())
                    .guestbookHostId(multiFcmSendDto.getGuestbookHostId())
                    .build();

            try {
                sendMessage(singleDto);
                successCount++;
            } catch (BaseException e) {
                failureCount++;
                log.error("[FCM 단일 전송 실패 - 멀티 전송 중] token: {}, message: {}", token, e.getMessage());
            }
        }

        log.info("[FCM 멀티 전송 결과] 성공: {}, 실패: {}", successCount, failureCount);

        if (failureCount > 0) {
            throw new BaseException(FcmResponseStatus.FCM_MULTICAST_ERROR, "일부 FCM 전송에 실패했습니다.");
        }
    }
}