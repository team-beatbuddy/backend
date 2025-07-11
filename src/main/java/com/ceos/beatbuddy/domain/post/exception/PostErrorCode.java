package com.ceos.beatbuddy.domain.post.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum PostErrorCode implements ApiCode {
    INVALID_POST_TYPE(HttpStatus.BAD_REQUEST,"포스트의 type이 올바르지 않습니다"),
    INVALID_SORT_TYPE(HttpStatus.BAD_REQUEST,"포스트의 sort_type이 올바르지 않습니다"),
    DUPLICATE_HASHTAG_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "해시태그는 중복될 수 없습니다."),
    INVALID_DTO_TYPE(HttpStatus.BAD_REQUEST, "잘못된 DTO TYPE입니다."),

    PIECE_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 조각입니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    NOT_FOUND_HASHTAG(HttpStatus.NOT_FOUND, "존재하지 않는 해시태그입니다."),
    POST_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다."),



    ;
    private final HttpStatus httpStatus;
    private final String message;


    PostErrorCode(HttpStatus httpStatus, String message) {
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
