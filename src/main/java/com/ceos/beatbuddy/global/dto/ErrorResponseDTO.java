package com.ceos.beatbuddy.global.dto;


import com.ceos.beatbuddy.global.ApiCode;
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


    public ErrorResponseDTO(ApiCode errorCode) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
    }

    public ErrorResponseDTO(ApiCode errorCode, String message) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.name();
        this.message = message;
    }

    public ErrorResponseDTO(ApiCode errorCode, Map<String, String> errors) {
        this.status = errorCode.getStatus().value();
        this.error = errorCode.getStatus().name();
        this.code = errorCode.name();
        this.message = errorCode.getMessage();
        this.errors = errors;
    }
}