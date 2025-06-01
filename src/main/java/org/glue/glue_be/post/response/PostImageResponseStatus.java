package org.glue.glue_be.post.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@AllArgsConstructor
public enum PostImageResponseStatus implements ResponseStatus {

	POST_IMAGE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "postImage 레코드를 찾을 수 없습니다"),

	;

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}
