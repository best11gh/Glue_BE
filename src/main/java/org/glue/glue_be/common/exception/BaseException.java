package org.glue.glue_be.common.exception;

import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;

@Getter
public class BaseException extends RuntimeException {

	private final ResponseStatus status;
	private final String detailMessage;

	public BaseException(ResponseStatus status) {
		super(status.getMessage());
		this.status = status;
		this.detailMessage = status.getMessage();
	}

	public BaseException(ResponseStatus status, String detailMessage) {
		super(detailMessage);
		this.status = status;
		this.detailMessage = detailMessage;
	}
	
	@Override
	public String getMessage() {
		return detailMessage;
	}
}