package org.glue.glue_be.common.exception;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.glue.glue_be.common.response.BaseResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {


    // RequestBody 유효성 검증
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ErrorValidationResult>> handleValidationExceptions(
            MethodArgumentNotValidException e) {
        ErrorValidationResult errorValidationResult = new ErrorValidationResult();

        for (FieldError fieldError : e.getBindingResult().getFieldErrors()) {
            errorValidationResult.addValidation(fieldError.getField(), fieldError.getDefaultMessage());
        }

        BaseResponse<ErrorValidationResult> response = new BaseResponse<>(
                HttpStatus.BAD_REQUEST,
                false,
                ErrorValidationResult.ERROR_MESSAGE,
                ErrorValidationResult.ERROR_STATUS_CODE,
                errorValidationResult
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // RequestParam, PathVariable 유효성 검증
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<ErrorValidationResult>> handleConstraintViolationException(
            ConstraintViolationException e) {
        ErrorValidationResult errorValidationResult = new ErrorValidationResult();

        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String field = violation.getPropertyPath().toString();
            String fieldName = field.contains(".") ? field.substring(field.lastIndexOf('.') + 1) : field;
            String message = violation.getMessage();
            errorValidationResult.addValidation(fieldName, message);
        }

        BaseResponse<ErrorValidationResult> response = new BaseResponse<>(
                HttpStatus.BAD_REQUEST,
                false,
                ErrorValidationResult.ERROR_MESSAGE,
                ErrorValidationResult.ERROR_STATUS_CODE,
                errorValidationResult
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // RequestParam 필수값 누락
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<ErrorValidationResult>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {

        ErrorValidationResult errorValidationResult = new ErrorValidationResult();

        String name = e.getParameterName();
        String type = e.getParameterType();

        String message = String.format("'%s'(%s 타입)가 누락되었습니다.", name, type);
        errorValidationResult.addValidation(name, message);

        BaseResponse<ErrorValidationResult> response = new BaseResponse<>(
                HttpStatus.BAD_REQUEST,
                false,
                ErrorValidationResult.ERROR_MESSAGE,
                ErrorValidationResult.ERROR_STATUS_CODE,
                errorValidationResult
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }


    // 커스텀 예외 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<BaseResponse<Void>> handleBaseExceptions(BaseException e) {
        BaseResponse<Void> response = new BaseResponse<>(e.getStatus(), e.getMessage());
        return ResponseEntity.status(e.getStatus().getHttpStatusCode()).body(response);
    }


}
