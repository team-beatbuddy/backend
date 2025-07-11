package com.ceos.beatbuddy.domain.venue.exception;


import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VenueErrorCode implements ApiCode {

    INVALID_VENUE_INFO(HttpStatus.BAD_REQUEST, "잘못된 베뉴 정보입니다."),
    VENUE_OVER_REQUEST(HttpStatus.BAD_REQUEST, "존재하는 베뉴의 개수보다 많은 개수를 요청했습니다"),
    VENUE_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 베뉴입니다."),
    INVALID_VENUE_IMAGE(HttpStatus.BAD_REQUEST, "잘못된 이미지 파일입니다."),
;
    private final HttpStatus httpStatus;
    private final String message;


    VenueErrorCode(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}
