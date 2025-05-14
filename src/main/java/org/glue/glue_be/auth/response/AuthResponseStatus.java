package org.glue.glue_be.auth.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum AuthResponseStatus implements ResponseStatus {

	EXPIRE_CODE(HttpStatus.NOT_FOUND, false, 400, "코드값이 만료됐습니다."),
	FALSE_CODE(HttpStatus.NOT_FOUND, false, 400, "코드값이 틀렸습니다."),
	FAIL_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "이메일 전송에 실패했습니다."),

	;


	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}



