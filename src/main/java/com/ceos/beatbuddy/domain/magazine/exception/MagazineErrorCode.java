package com.ceos.beatbuddy.domain.magazine.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MagazineErrorCode implements ApiCode {
    CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER(HttpStatus.FORBIDDEN, "글을 작성할 수 없는 유저입니다."),
    MAGAZINE_NOT_EXIST(HttpStatus.NOT_FOUND, "해당 매거진을 찾을 수 없습니다."),
    INVALID_ORDER_IN_HOME(HttpStatus.BAD_REQUEST, "홈에서의 순서가 잘못되었습니다. 고정된 매거진은 1 이상의 순서를 가져야 합니다."),
    DUPLICATE_ORDER_IN_HOME(HttpStatus.BAD_REQUEST, "홈에서의 순서가 중복되었습니다. 이미 존재하는 순서를 사용하고 있습니다."),


    ;
//    POST_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 포스트입니다."),
//    MEMBER_NOT_MATCH(HttpStatus.BAD_REQUEST, "게시물의 생성자가 아닙니다."),
//    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
//    VENUE_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 베뉴입니다"),
//    INVALID_POST_TYPE(HttpStatus.BAD_REQUEST,"포스트의 type이 올바르지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;


    MagazineErrorCode(HttpStatus httpStatus, String message) {
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
