package org.glue.glue_be.post.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum PostResponseStatus implements ResponseStatus {

	/**
	 * 게시글 관련 에러
	 */
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, false, 600, "존재하지 않는 게시글입니다");

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}
