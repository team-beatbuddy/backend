package com.ceos.beatbuddy.global.code;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
@RequiredArgsConstructor
@Getter
public enum ErrorCode implements ApiCode {
    /**
     * 400
     */
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    NOT_VALID_ADDRESS(HttpStatus.BAD_REQUEST, "잘못된 주소입니다."),

    /**
     * 401
     */
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_ACCESS_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 액세스 토큰입니다."),
    TOKEN_MISSING(HttpStatus.UNAUTHORIZED, "요청 헤더에 토큰이 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),
    WRONG_PASSWORD(HttpStatus.UNAUTHORIZED, "잘못된 비밀번호입니다."),


    /**
     * 403
     * */
    UNAUTHORIZED_MEMBER(HttpStatus.FORBIDDEN, "해당 작업에 대한 권한이 없습니다."),



    /**
     * 404
     */
    NOT_FOUND_SCRAP(HttpStatus.NOT_FOUND, "기존에 스크랩하지 않았습니다. 스크랩을 취소할 수 없습니다."),
    NOT_FOUND_LIKE(HttpStatus.NOT_FOUND, "기존에 좋아요를 누르지 않았습니다. 좋아요를 취소할 수 없습니다."),
    NOT_FOUND_COMMENT(HttpStatus.NOT_FOUND, "해당 댓글을 찾을 수 없습니다."),
    // 댓글이 속하지 않을 때
    NOT_FOUND_COMMENT_IN_EVENT(HttpStatus.NOT_FOUND, "해당 댓글이 이벤트에 속하지 않습니다."),

    /**
     * 409
     * */
    ALREADY_LIKED(HttpStatus.CONFLICT, "이미 좋아요를 눌렀습니다."),
    ALREADY_SCRAPPED(HttpStatus.CONFLICT, "이미 스크랩을 눌렀습니다."),

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
    /**
     * 500
     * */
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 삭제에 실패했습니다."),

    ;
    private final HttpStatus status;
    private final String message;
}