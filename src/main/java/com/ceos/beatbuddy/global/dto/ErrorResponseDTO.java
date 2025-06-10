package com.ceos.beatbuddy.global.dto;

<<<<<<< Updated upstream
<<<<<<< Updated upstream
import com.ceos.beatbuddy.global.ErrorCode;
=======
=======
>>>>>>> Stashed changes
import com.ceos.beatbuddy.global.ApiErrorCode;
import com.ceos.beatbuddy.global.code.ErrorCode;
>>>>>>> Stashed changes
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponseDTO {
    private int status;
    private String error;
    private String code;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, String> errors;


    public ErrorResponseDTO(ApiErrorCode errorCode) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public ErrorResponseDTO(ApiErrorCode errorCode, String message) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.code = errorCode.name();
        this.message = message;
    }

    public ErrorResponseDTO(ApiErrorCode errorCode, Map<String, String> errors) {
        this.status = errorCode.getHttpStatus().value();
        this.error = errorCode.getHttpStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }
}