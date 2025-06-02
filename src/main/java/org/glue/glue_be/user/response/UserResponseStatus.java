package org.glue.glue_be.user.response;

import lombok.*;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.*;

@Getter
@AllArgsConstructor
public enum UserResponseStatus implements ResponseStatus {

    USER_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 사용자입니다"),
    ALREADY_EXISTS(HttpStatus.CONFLICT, false, 409, "이미 사용자가 존재합니다."),
    NOT_OPEN(HttpStatus.FORBIDDEN, false, 403, "해당 정보는 사용자가 공개하지 않는 정보입니다"),
    INVALID_ROLE(HttpStatus.BAD_REQUEST, false, 400, "잘못된 사용자 역할입니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
} 