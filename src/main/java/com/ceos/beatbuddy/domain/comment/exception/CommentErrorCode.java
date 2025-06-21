package com.ceos.beatbuddy.domain.comment.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommentErrorCode implements ApiCode {
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 댓글입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String message;

    CommentErrorCode(HttpStatus httpStatus, String message) {
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
