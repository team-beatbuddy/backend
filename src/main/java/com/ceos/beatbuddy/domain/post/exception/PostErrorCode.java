package com.ceos.beatbuddy.domain.post.exception;

import com.ceos.beatbuddy.global.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostErrorCode implements ApiErrorCode {
    POST_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다."),
    MEMBER_NOT_MATCH(HttpStatus.BAD_REQUEST, "게시물의 생성자가 아닙니다."),
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    VENUE_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 베뉴입니다"),
    INVALID_POST_TYPE(HttpStatus.BAD_REQUEST,"포스트의 type이 올바르지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;


    PostErrorCode(HttpStatus httpStatus, String message) {
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
