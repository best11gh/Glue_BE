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
	POST_NOT_FOUND(HttpStatus.NOT_FOUND, false, 600, "존재하지 않는 게시글입니다"),
	POST_CANNOT_BUMP_YET(HttpStatus.NOT_EXTENDED, false, 601 , "이전 끌올에서 3일이 지나야 끌올할 수 있습니다." ),
	POST_NOT_AUTHOR(HttpStatus.FORBIDDEN, false, 602, "게시글 작성자만 접근할 수 있습니다."),

	;

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}
