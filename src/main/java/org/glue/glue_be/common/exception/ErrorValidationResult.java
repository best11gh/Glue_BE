package org.glue.glue_be.common.exception;

import java.util.*;

public class ErrorValidationResult {
    public static int ERROR_STATUS_CODE = 400;
    public static String ERROR_MESSAGE = "유효성 검사에 실패했습니다.";
    public final Map<String, String> validation = new HashMap<>();

    public void addValidation(String fieldName, String errorMessage) {
        this.validation.put(fieldName, errorMessage);
    }
}
