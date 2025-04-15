package org.glue.glue_be.common.response;

import org.springframework.http.HttpStatusCode;

/**
 * 모든 응답 상태 enum이 구현해야 하는 인터페이스
 */
public interface ResponseStatus {
    HttpStatusCode getHttpStatusCode();
    boolean isSuccess();
    int getCode();
    String getMessage();
} 