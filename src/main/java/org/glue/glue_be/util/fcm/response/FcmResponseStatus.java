package org.glue.glue_be.util.fcm.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum FcmResponseStatus implements ResponseStatus {

    FCM_SEND_SUCCESS     (HttpStatus.OK,                   true,  200, "FCM 단일 전송에 성공했습니다"),
    FCM_SEND_ERROR       (HttpStatus.INTERNAL_SERVER_ERROR,false,500, "FCM 단일 전송에 실패했습니다"),

    FCM_MULTICAST_SUCCESS(HttpStatus.OK,                   true,  200, "단체 FCM 전송에 성공했습니다"),
    FCM_MULTICAST_ERROR  (HttpStatus.INTERNAL_SERVER_ERROR,false,500, "단체 FCM 전송에 실패했습니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
