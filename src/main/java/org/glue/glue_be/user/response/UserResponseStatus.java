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
    CANNOT_DELETE_WITH_ACTIVE_MEETINGS(HttpStatus.CONFLICT, false, 409, "호스트로서 진행 중인 모임이 있어 탈퇴할 수 없습니다");



    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
} 