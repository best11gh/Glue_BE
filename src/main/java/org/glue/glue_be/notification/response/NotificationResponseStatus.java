package org.glue.glue_be.notification.response;


import lombok.*;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.*;

@Getter
@AllArgsConstructor
public enum NotificationResponseStatus implements ResponseStatus {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 알림입니다"),
    NOT_OWNER_OF_NOTIFICATION(HttpStatus.FORBIDDEN, false, 403, "알림의 주인만 접근할 수 있습니다"),

    REMINDER_SCHEDULE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "리마인더 예약에 실패했습니다."),
    REMINDER_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "리마인더 삭제에 실패했습니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
