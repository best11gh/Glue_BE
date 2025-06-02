package org.glue.glue_be.meeting.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum MeetingResponseStatus implements ResponseStatus {

    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 모임입니다"),
    MEETING_FULL(HttpStatus.BAD_REQUEST, false, 400, "모임이 꽉 찼습니다"),
    MIN_OVER_MAX(HttpStatus.BAD_REQUEST, false, 400, "최소 인원이 최대 인원보다 클 수 없습니다"),
    INVALID_MEETING_TIME(HttpStatus.BAD_REQUEST, false, 400, "모임 시간은 현재 시간 이후여야 합니다"),
    ALREADY_JOINED(HttpStatus.CONFLICT, false, 409, "이미 모임에 참여 중입니다"),
    NON_PARTICIPANT_INVITATION(HttpStatus.FORBIDDEN, false, 403, "모임 참가자만 초대장을 생성할 수 있습니다"),
    NOT_HOST_PERMISSION(HttpStatus.FORBIDDEN, false, 403, "모임 주최자만 이 작업을 수행할 수 있습니다"),
    NOT_JOINED(HttpStatus.FORBIDDEN, false, 403, "모임에 참여하지 않은 사용자입니다"),
    MEETING_HOST_DELETED(HttpStatus.FORBIDDEN, false, 403, "탈퇴한 사용자가 주최한 모임에는 참여할 수 없습니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
} 