package org.glue.glue_be.post.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum PostResponseStatus implements ResponseStatus {

	POST_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 게시글입니다"),
	POST_CANNOT_BUMP_MORE(HttpStatus.TOO_MANY_REQUESTS, false, 400 , "가능한 끌올 횟수를 모두 소진했습니다." ),
	POST_NOT_AUTHOR(HttpStatus.FORBIDDEN, false, 403, "게시글 작성자만 접근할 수 있습니다.");

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}
