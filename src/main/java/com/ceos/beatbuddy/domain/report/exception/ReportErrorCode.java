package com.ceos.beatbuddy.domain.report.exception;

import com.ceos.beatbuddy.global.ApiCode;
import org.springframework.http.HttpStatus;

public enum ReportErrorCode implements ApiCode {
    // 존재하지 않는 report type
    INVALID_REPORT_TARGET_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 신고 대상 타입입니다."),
    TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, "신고 대상이 존재하지 않습니다."),
    ;


    private final HttpStatus httpStatus;
    private final String message;


    ReportErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
