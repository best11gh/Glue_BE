package org.glue.glue_be.common.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static org.glue.glue_be.common.response.BaseResponseStatus.SUCCESS;

public record BaseResponse<T>(HttpStatusCode httpStatus, Boolean isSuccess, String message, int code, T result) {

	/**
	 * 필요값 : Http상태코드, 성공여부, 메시지, 에러코드, 결과값
	 */

	// 요청에 성공한 경우 -> return 객체가 필요한 경우
	public BaseResponse(T result) {
		this(
			HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), result);
	}

	// 요청에 성공한 경우 -> return 객체가 필요 없는 경우
	public BaseResponse() {
		this(HttpStatus.OK, true, SUCCESS.getMessage(), SUCCESS.getCode(), null);
	}

	// 요청 실패한 경우 (모든 ResponseStatus 구현체 사용 가능)
	public BaseResponse(ResponseStatus status) {
		this(status.getHttpStatusCode(), status.isSuccess(), status.getMessage(), status.getCode(), null);
	}
	
	// 요청 실패한 경우 (상세 메시지 포함)
	public BaseResponse(ResponseStatus status, String detailMessage) {
		this(status.getHttpStatusCode(), status.isSuccess(), detailMessage, status.getCode(), null);
	}
}
