package org.glue.glue_be.guestbook.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@AllArgsConstructor
public enum GuestBookResponseStatus implements ResponseStatus {

    GUESTBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 방명록입니다"),
    GUEST_BOOK_SELF_WRITE_NOT_ALLOWED(HttpStatus.FORBIDDEN, false, 403, "자신에게 방명록을 작성할 수 없습니다."),
    GUEST_BOOK_REPLY_NOT_ALLOWED(HttpStatus.FORBIDDEN, false, 403, "방명록 댓글은 호스트만 작성할 수 있습니다."),
    GUESTBOOK_NOT_AUTHOR(HttpStatus.FORBIDDEN, false, 403, "해당 방명록의 작성자만 접근할 수 있습니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
