package org.glue.glue_be.notice.response;


import lombok.*;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.*;

@Getter
@AllArgsConstructor
public enum NoticeResponseStatus implements ResponseStatus {

    NOTICE_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 방명록입니다");


    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}

