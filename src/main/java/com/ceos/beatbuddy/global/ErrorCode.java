package com.ceos.beatbuddy.global;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    /**
     * 400
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_VALID_ADDRESS(HttpStatus.BAD_REQUEST, "잘못된 주소입니다."),

//    /**
//     * 401
//     */
//    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
//    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
//    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "요청 헤더에 토큰이 없습니다."),
//    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
//    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),
//
//    /**
//     * 404
//     */
//    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
//    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "반려동물 정보를 찾을 수 없습니다."),
//    PARENT_MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이전 메시지가 존재하지 않습니다."),
//    RECOMMEND_STRATEGY_NOT_FOUND(HttpStatus.NOT_FOUND, "추천한 전략이 존재하지 않습니다."),
//
//    /**
//     * 406
//     * */
//    DISEASE_INVALID(HttpStatus.NOT_ACCEPTABLE, "질병을 잘못 입력했습니다."),
//    TYPE_INVALID(HttpStatus.NOT_ACCEPTABLE, "동물 타입을 잘못 입력했습니다."),
//    GENDER_INVALID(HttpStatus.NOT_ACCEPTABLE, "성별을 잘못 입력했습니다."),
//    HEALTH_INVALID(HttpStatus.NOT_ACCEPTABLE, "건강 상태를 잘못 입력했습니다."),
//    NEUTERED_INVALID(HttpStatus.NOT_ACCEPTABLE, "중성화 여부를 잘못 입력했습니다."),
//    STATE_INVALID(HttpStatus.NOT_ACCEPTABLE, "상태를 잘못 입력했습니다."),
//    DATE_INVALID(HttpStatus.NOT_ACCEPTABLE, "오늘보다 미래의 날짜를 선택할 수 없습니다."),
//
//    /**
//     * 409
//     */
//    DUPLICATE_USERNAME(HttpStatus.CONFLICT, "중복된 아이디입니다."),


    ;
    private final HttpStatus status;
    private final String message;
}