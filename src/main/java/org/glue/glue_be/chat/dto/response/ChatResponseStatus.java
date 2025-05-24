package org.glue.glue_be.chat.dto.response;

import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public enum ChatResponseStatus implements ResponseStatus {
    // Success responses
    CHATROOM_CREATED(HttpStatus.CREATED, true, 200, "채팅방을 성공적으로 생성하였습니다"),
    CHATROOM_FOUND(HttpStatus.OK, true, 200, "이미 있는 채팅방을 반환합니다"),
    CHATROOM_JOINED(HttpStatus.OK, true, 200, "채팅방에 성공적으로 참여하였습니다"),
    CHATROOM_LEFT(HttpStatus.OK, true, 200, "채팅방에서 성공적으로 퇴장하였습니다"),
    MESSAGE_SENT(HttpStatus.CREATED, true, 201, "메시지가 성공적으로 전송되었습니다"),

    // Error responses
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 채팅방입니다"),
    MEETING_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 미팅입니다"),
    USER_NOT_MEMBER(HttpStatus.FORBIDDEN, false, 403, "채팅방에 참여하지 않은 사용자입니다"),
    NOTIFICATION_TOGGLED_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "모임 토글이 정상적으로 이루어지지 않았습니다"),
    HOST_CANNOT_LEAVE(HttpStatus.FORBIDDEN, false, 403, "모임 호스트는 채팅방을 나갈 수 없습니다"),
    ONLY_HOST_CAN_CREATE(HttpStatus.FORBIDDEN, false, 403, "미팅 호스트만 새 채팅방을 생성할 수 있습니다"),
    INVALID_DM_USER_COUNT(HttpStatus.BAD_REQUEST, false, 400, "DM 채팅방은 본인을 포함한 정확히 2명의 사용자가 필요합니다"),
    INVALID_GROUP_USER(HttpStatus.BAD_REQUEST, false, 400, "현재 사용자가 포함되어 있어야 합니댜."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "메시지를 찾을 수 없습니다"),
    CHATROOM_CREATION_FAILED_NO_MEETING(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "유효하지 않은 미팅이므로 채팅방 생성에 실패하였습니다"),
    CHATROOM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "채팅방 생성에 실패하였습니다"),
    MESSAGE_SENDING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "메시지 전송에 실패하였습니다"),
    MESSAGES_READ_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "메시지 읽음 처리 실패하였습니다"),
    INVITATION_STATUS_ERROR(HttpStatus.BAD_REQUEST, false, 400, "초대 상태를 확인할 수 없습니다");

    private final HttpStatusCode httpStatusCode;
    private final boolean success;
    private final int code;
    private final String message;

    ChatResponseStatus(HttpStatusCode httpStatusCode, boolean success, int code, String message) {
        this.httpStatusCode = httpStatusCode;
        this.success = success;
        this.code = code;
        this.message = message;
    }

    @Override
    public HttpStatusCode getHttpStatusCode() {
        return httpStatusCode;
    }

    @Override
    public boolean isSuccess() {
        return success;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}