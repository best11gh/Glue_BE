package org.glue.glue_be.report.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
public enum ReportResponseStatus implements ResponseStatus {

    // 신고
    REPORT_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 신고입니다"),
    ALREADY_HANDLED_REPORT(HttpStatus.BAD_REQUEST, false, 400, "이미 처리된 신고입니다"),


    // 신고 사유
    REPORT_REASON_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "존재하지 않는 신고 사유입니다"),
    DUPLICATE_REPORT_REASON(HttpStatus.CONFLICT, false, 409, "이미 존재하는 신고 사유입니다");


    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}
