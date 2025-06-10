package com.ceos.beatbuddy.domain.member.exception;

import com.ceos.beatbuddy.domain.member.dto.error.MemberMoodErrorCodeResponse;
import com.ceos.beatbuddy.global.ApiErrorCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberMoodErrorCode implements ApiErrorCode {
    MEMBER_MOOD_OVER_REQUEST(HttpStatus.BAD_REQUEST, "존재하는 선호 분위기 벡터보다 요청 수가 많습니다"),
    MEMBER_MOOD_ONLY_ONE(HttpStatus.BAD_REQUEST, "선호 분위기 벡터가 1개밖에 없어서 삭제할 수 없습니다"),
    MEMBER_MOOD_NOT_EXIST(HttpStatus.NOT_FOUND, "선호 분위기가 존재하지 않습니다");

    private final HttpStatus httpStatus;
    private final String message;


    MemberMoodErrorCode(HttpStatus httpStatus, String message) {
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
