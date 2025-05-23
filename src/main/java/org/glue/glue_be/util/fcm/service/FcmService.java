package org.glue.glue_be.util.fcm.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.glue.glue_be.common.exception.BaseException;
import org.glue.glue_be.util.fcm.dto.FcmSendDto;
import org.glue.glue_be.util.fcm.dto.MultiFcmSendDto;
import org.glue.glue_be.util.fcm.response.FcmResponseStatus;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FcmService {

    public void sendMessage(FcmSendDto fcmSendDto) {
        Message message = Message.builder()
                .setNotification(Notification.builder()
                        .setTitle(fcmSendDto.getTitle())
                        .setBody(fcmSendDto.getBody())
                        .build())
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
        MulticastMessage message = MulticastMessage.builder()
                .setNotification(Notification.builder()
                        .setTitle(multiFcmSendDto.getTitle())
                        .setBody(multiFcmSendDto.getBody())
                        .build())
                .addAllTokens(multiFcmSendDto.getTokens())
                .build();

        try {
            FirebaseMessaging.getInstance().sendMulticast(message);
        } catch (FirebaseMessagingException e) {
            log.error("[FCM 단체 전송 실패] tokens: {}, title: {}, body: {}, message: {}",
                    multiFcmSendDto.getTokens(),
                    multiFcmSendDto.getTitle(),
                    multiFcmSendDto.getBody(),
                    e.getMessage(),
                    e
            );
            throw new BaseException(FcmResponseStatus.FCM_MULTICAST_ERROR, "FCM 다중 전송에 실패했습니다.");
        }
    }
}
