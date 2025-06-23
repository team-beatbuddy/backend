package com.ceos.beatbuddy.domain.event.exception;

import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.global.ApiCode;
import org.springframework.http.HttpStatus;

public enum EventErrorCode implements ApiCode {
    NOT_FOUND_EVENT(HttpStatus.NOT_FOUND, "존재하지 않는 이벤트입니다."),
    INVALID_GENDER(HttpStatus.BAD_REQUEST, "성별 값이 올바르지 않습니다. (MALE, FEMALE, NONE 중 하나여야 합니다.)"),
    ALREADY_ATTENDANCE_EVENT(HttpStatus.CONFLICT, "이미 참여 신청한 이벤트입니다."),
    MISSING_NAME(HttpStatus.BAD_REQUEST, "이름 입력은 필수입니다."),
    MISSING_GENDER(HttpStatus.BAD_REQUEST, "성별 입력은 필수입니다."),
    MISSING_PHONE(HttpStatus.BAD_REQUEST, "핸드폰 번호 입력은 필수입니다."),
    MISSING_TOTAL_COUNT(HttpStatus.BAD_REQUEST, "동행인원 입력은 필수입니다."),
    MISSING_PAYMENT(HttpStatus.BAD_REQUEST, "지불 완료 입력은 필수입니다."),
    MISSING_SNS_ID_OR_TYPE(HttpStatus.BAD_REQUEST, "SNS ID 또는 TYPE 입력은 필수입니다."),
    FORBIDDEN_EVENT_ACCESS(HttpStatus.FORBIDDEN, "해당 이벤트에 대한 접근 권한이 없습니다."),
    CANNOT_ADD_EVENT_UNAUTHORIZED_MEMBER(HttpStatus.FORBIDDEN, "글을 작성할 수 없는 유저입니다."),
    NEED_DEPOSIT_INFO(HttpStatus.BAD_REQUEST, "예약금에 관련된 정보가 필요합니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    INVALID_RECEIVE_INFO_CONFIGURATION(HttpStatus.BAD_REQUEST, "receiveInfo가 false일 때 다른 수집 항목은 true일 수 없습니다."),
    ATTENDANCE_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이벤트에 대한 참석 정보를 찾을 수 없습니다.")

    ;
    private final HttpStatus httpStatus;
    private final String message;


    EventErrorCode(HttpStatus httpStatus, String message) {
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
