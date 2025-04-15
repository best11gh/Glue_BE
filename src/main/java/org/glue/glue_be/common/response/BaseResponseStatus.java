package org.glue.glue_be.common.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum BaseResponseStatus implements ResponseStatus {

	/**
	 * 200: 요청 성공
	 **/
	SUCCESS(HttpStatus.OK, true, 200, "요청에 성공하였습니다."),

	/**
	 * 400 : security 에러
	 */
	WRONG_JWT_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "다시 로그인 해주세요"),
	NO_SIGN_IN(HttpStatus.UNAUTHORIZED, false, 402, "로그인을 먼저 진행해주세요"),
	NO_ACCESS_AUTHORITY(HttpStatus.FORBIDDEN, false, 403, "접근 권한이 없습니다"),
	FORBIDDEN(HttpStatus.FORBIDDEN, false, 404, "권한이 없습니다"),

	/**
	 * 900: 기타 에러
	 */
	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 900, "Internal server error"),
	INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, false, 901, "잘못된 입력값입니다. 다시 확인해주세요."),
	LOGGING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 903, "로그 기록 중 에러가 발생했습니다.");

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}