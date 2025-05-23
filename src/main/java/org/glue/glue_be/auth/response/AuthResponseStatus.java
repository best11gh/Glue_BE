package org.glue.glue_be.auth.response;


import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum AuthResponseStatus implements ResponseStatus {

	// 소셜 로그인 관련
	SOCIAL_API_REQUEST_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "소셜 로그인 서버와의 통신에 실패했습니다."),

	// JWT 관련
	JWT_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, false, 401, "JWT 인증에 실패했습니다."),

	// 애플 관련
	INVALID_AUTHORIZATION_CODE(HttpStatus.UNAUTHORIZED, false, 401, "인증 코드가 유효하지 않습니다."),
	FAIL_APPLE_PRIVATE_KEY(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "Apple 개인 키 처리에 실패했습니다."),
	INVALID_ID_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "Apple ID 토큰이 유효하지 않습니다."),
	EXPIRED_ID_TOKEN(HttpStatus.UNAUTHORIZED, false, 401, "Apple ID 토큰이 만료되었습니다."),
	SIGNATURE_VERIFICATION_FAILED(HttpStatus.UNAUTHORIZED, false, 401, "Apple 서명 검증에 실패했습니다."),

	// 이메일 인증 관련
	EXPIRE_CODE(HttpStatus.BAD_REQUEST, false, 400, "인증 코드가 만료되었습니다."),
	FALSE_CODE(HttpStatus.BAD_REQUEST, false, 400, "인증 코드가 올바르지 않습니다."),
	FAIL_EMAIL(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "이메일 인증 코드 전송에 실패했습니다.");

	private final HttpStatusCode httpStatusCode;
	private final boolean isSuccess;
	private final int code;
	private final String message;
}



