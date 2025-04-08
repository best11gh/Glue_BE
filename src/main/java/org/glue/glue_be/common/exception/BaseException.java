package org.glue.glue_be.common.exception;


import lombok.Getter;
import org.glue.glue_be.common.response.BaseResponseStatus;


@Getter
public class BaseException extends RuntimeException {

	private final BaseResponseStatus status;


	public BaseException(BaseResponseStatus status) {
		this.status = status;
	}

}