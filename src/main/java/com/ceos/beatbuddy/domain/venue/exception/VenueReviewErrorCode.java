package com.ceos.beatbuddy.domain.venue.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum VenueReviewErrorCode implements ApiCode {

    VENUE_REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 베뉴 리뷰입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String message;


    VenueReviewErrorCode(HttpStatus httpStatus, String message) {
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