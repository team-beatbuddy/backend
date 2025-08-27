package com.ceos.beatbuddy.domain.follow.exception;

import com.ceos.beatbuddy.global.ApiCode;
import org.springframework.http.HttpStatus;

public enum FollowErrorCode implements ApiCode {

    CANNOT_FOLLOW_SELF(HttpStatus.NOT_ACCEPTABLE, "자기 자신은 팔로우할 수 없습니다."),
    ALREADY_FOLLOWED(HttpStatus.CONFLICT,"이미 팔로우한 대상입니다."),
    FOLLOW_NOT_FOUND(HttpStatus.NOT_FOUND,"팔로우 관계가 존재하지 않습니다."),
    FOLLOWING_TARGET_NOT_FOUND(HttpStatus.NOT_FOUND,"팔로우 대상이 존재하지 않습니다."),
    CANNOT_FOLLOW_BLOCKED_USER(HttpStatus.FORBIDDEN, "차단한 사용자는 팔로우할 수 없습니다.");

    ;
    private final HttpStatus httpStatus;
    private final String message;

    FollowErrorCode(HttpStatus httpStatus, String message) {
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
