package com.ceos.beatbuddy.domain.coupon.exception;

import com.ceos.beatbuddy.global.ApiCode;
import org.springframework.http.HttpStatus;

public enum CouponErrorCode implements ApiCode {
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_ALREADY_RECEIVED(HttpStatus.CONFLICT, "이미 받은 쿠폰입니다."),
    COUPON_EXPIRED(HttpStatus.BAD_REQUEST, "쿠폰이 만료되었습니다."),
    COUPON_SOLD_OUT(HttpStatus.BAD_REQUEST, "쿠폰이 모두 소진되었습니다."),
    COUPON_NOT_ACTIVE(HttpStatus.BAD_REQUEST, "활성화되지 않은 쿠폰입니다."),
    COUPON_ALREADY_RECEIVED_TODAY(HttpStatus.CONFLICT, "이미 오늘 해당 쿠폰을 수령하셨습니다."),
    COUPON_QUOTA_NOT_INITIALIZED(HttpStatus.BAD_REQUEST, "쿠폰의 수량이 초기화되지 않았습니다."),
    COUPON_QUOTA_SOLD_OUT(HttpStatus.BAD_REQUEST, "쿠폰의 수량이 모두 소진되었습니다."),
    COUPON_INVALID_POLICY(HttpStatus.BAD_REQUEST, "유효하지 않은 쿠폰 정책입니다."),
    COUPON_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 쿠폰입니다."),
    COUPON_DISABLED(HttpStatus.BAD_REQUEST, "사용할 수 없는 쿠폰입니다."),
    MEMBER_COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원의 쿠폰을 찾을 수 없습니다."),
    COUPON_RECEIVE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "쿠폰 수령 한도를 초과했습니다."),
    COUPON_USE_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "쿠폰 사용 한도를 초과했습니다."),
    COUPON_SAME_VENUE_USE_EXCEEDED(HttpStatus.BAD_REQUEST, "같은 업장에서 쿠폰 사용 한도를 초과했습니다."),
    // 쿠폰 관련 서버 오류
    COUPON_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "쿠폰 관련 서버 오류가 발생했습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String message;


    CouponErrorCode(HttpStatus httpStatus, String message) {
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
