package com.ceos.beatbuddy.global;

import com.ceos.beatbuddy.global.dto.ErrorResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        log.warn("Validation Error: {}", errors);
            ApiErrorCode badRequest = new ApiErrorCode() {
            @Override public HttpStatus getHttpStatus() { return HttpStatus.BAD_REQUEST; }
            @Override public String getMessage() { return "요청 값이 유효하지 않습니다."; }
            @Override public String name() { return "BAD_REQUEST_VALIDATION"; }
        };
        return ResponseEntity
                .status(badRequest.getHttpStatus())
                .body(new ErrorResponseDTO(badRequest, errors));
    }

    @ExceptionHandler(ResponseException.class)
    protected ResponseEntity<ErrorResponseDTO> handleResponseException(final ResponseException e) {
        ApiErrorCode apiErrorCode = e.getApiErrorCode();

        log.error("Custom Exception Occurred: [Code: {}, Message: {}]", apiErrorCode.name(), apiErrorCode.getMessage(), e);

        return ResponseEntity
                .status(apiErrorCode.getHttpStatus())
                .body(new ErrorResponseDTO(apiErrorCode));
    }

    // Generic fallback for any other unexpected exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleGenericException(Exception e) {
        log.error("An unexpected server error occurred: {}", e.getMessage(), e);
        ApiErrorCode internalServerError = new ApiErrorCode() {
            @Override public HttpStatus getHttpStatus() { return HttpStatus.INTERNAL_SERVER_ERROR; }
            @Override public String getMessage() { return "예상치 못한 서버 오류가 발생했습니다."; }
            @Override public String name() { return "INTERNAL_SERVER_ERROR"; }
        };
        return ResponseEntity
                .status(internalServerError.getHttpStatus())
                .body(new ErrorResponseDTO(internalServerError));
    }
}