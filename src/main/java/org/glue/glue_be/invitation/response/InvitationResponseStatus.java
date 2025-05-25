package org.glue.glue_be.invitation.response;

import lombok.*;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum InvitationResponseStatus implements ResponseStatus {

    INVITATION_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "초대장을 찾을 수 없습니다"),
    INVITATION_EXPIRED(HttpStatus.BAD_REQUEST, false, 400, "만료된 초대장입니다"),
    INVITATION_FULLY_USED(HttpStatus.BAD_REQUEST, false, 400, "최대 사용 인원을 초과했습니다"),
    INVITATION_INVALID(HttpStatus.BAD_REQUEST, false, 400, "유효하지 않은 초대장입니다"),
    INVITATION_ALREADY_JOINED(HttpStatus.CONFLICT, false, 409, "이미 참여한 미팅입니다"),
    INVITATION_NOT_FOR_USER(HttpStatus.FORBIDDEN, false, 403, "이 초대장은 다른 사용자를 위한 것입니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
} 