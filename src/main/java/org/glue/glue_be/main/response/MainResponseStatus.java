package org.glue.glue_be.main.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.glue.glue_be.common.response.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;


@Getter
@AllArgsConstructor
public enum MainResponseStatus implements ResponseStatus {

    // 버전 관련
    VERSION_REQUIRED(HttpStatus.BAD_REQUEST, false, 400, "버전 입력이 필수입니다."),
    NO_ACTIVE_DEPLOY_VERSION(HttpStatus.NOT_FOUND, false, 404, "활성화된 배포 버전이 없습니다. 먼저 배포 버전을 설정해주세요."),
    VERSION_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "해당 버전을 찾을 수 없습니다."),
    NO_CAROUSELS_FOR_VERSION(HttpStatus.BAD_REQUEST, false, 400, "해당 버전에 등록된 캐러셀이 없습니다."),

    // 파일 관련
    FILE_NAME_REQUIRED(HttpStatus.BAD_REQUEST, false, 400, "파일명 입력이 필수입니다."),
    INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, false, 400, "유효하지 않은 파일명입니다."),

    // 캐러셀 관련
    CAROUSEL_NOT_FOUND(HttpStatus.NOT_FOUND, false, 404, "캐러셀을 찾을 수 없습니다."),

    // 일반 에러
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, false, 500, "서버 내부 오류가 발생했습니다.");

    private final HttpStatusCode httpStatusCode;
    private final boolean isSuccess;
    private final int code;
    private final String message;
}