package com.ceos.beatbuddy.domain.heartbeat.exception;

import com.ceos.beatbuddy.domain.heartbeat.dto.HeartbeatErrorCodeResponse;
import com.ceos.beatbuddy.global.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum HeartbeatErrorCode implements ApiErrorCode {

    HEARTBEAT_ALREADY_EXIST(HttpStatus.CONFLICT, "사용자가 이미 해당 베뉴에 하트비트를 추가해놓은 상태입니다"),
    HEARTBEAT_NOT_EXIST(HttpStatus.NOT_FOUND, "해당되는 Heartbeat가 없습니다 (사용자가 해당 베뉴에 Heartbeat를 누른 기록이 없습니다)");

    private final HttpStatus httpStatus;
    private final String message;


    HeartbeatErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    @Override
    public HttpStatus getHttpStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
