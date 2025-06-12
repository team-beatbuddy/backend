package com.ceos.beatbuddy.domain.venue.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VenueMoodErrorCode implements ApiCode {

    INVALID_VENUE_MOOD_INFO(HttpStatus.BAD_REQUEST, "잘못된 베뉴 무드 정보입니다."),
    VENUE_MOOD_NOT_EXIST(HttpStatus.NOT_FOUND, "베뉴 무드가 존재하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String message;


    VenueMoodErrorCode(HttpStatus httpStatus, String message) {
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
