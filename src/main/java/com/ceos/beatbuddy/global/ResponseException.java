package com.ceos.beatbuddy.global;

import org.springframework.http.HttpStatus;

public class ResponseException  extends RuntimeException {
    private final HttpStatus status; // This will be derived from apiCode
    private final ApiCode apiCode; // <-- NEW: Store the generic API error code


    protected ResponseException(ApiCode apiCode) {
        super(apiCode.getMessage()); // Use the message from the ApiCode
        this.status = apiCode.getStatus();
        this.apiCode = apiCode;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public ApiCode getApiCode() {
        return apiCode;
    }
}