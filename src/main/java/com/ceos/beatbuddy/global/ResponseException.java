package com.ceos.beatbuddy.global;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public class ResponseException  extends RuntimeException {
    private final HttpStatus status; // This will be derived from apiErrorCode
    private final ApiErrorCode apiErrorCode; // <-- NEW: Store the generic API error code


    protected ResponseException(ApiErrorCode apiErrorCode) {
        super(apiErrorCode.getMessage()); // Use the message from the ApiErrorCode
        this.status = apiErrorCode.getHttpStatus();
        this.apiErrorCode = apiErrorCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiErrorCode getApiErrorCode() {
        return apiErrorCode;
    }
}