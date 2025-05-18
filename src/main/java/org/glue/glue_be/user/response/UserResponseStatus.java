package org.glue.glue_be.user.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum UserResponseStatus implements ResponseStatus {
    
    /**
     * 사용자 관련 에러
     */
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, false, 501, "존재하지 않는 사용자입니다"),
    ALREADY_EXISTS(HttpStatus.CONFLICT, false, 502, "이미 사용자가 존재합니다."),

    ;

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
} 