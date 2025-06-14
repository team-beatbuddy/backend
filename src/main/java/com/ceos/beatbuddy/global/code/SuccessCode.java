package com.ceos.beatbuddy.global.code;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SuccessCode implements ApiCode {
    /**
     * User
     */
    SUCCESS_REISSUE(HttpStatus.OK, "토큰 재발급을 성공했습니다. 헤더 토큰을 확인하세요."),
    SUCCESS_REGISTER(HttpStatus.OK, "회원가입을 성공했습니다."),
    SUCCESS_LOGIN(HttpStatus.OK, "로그인을 성공했습니다. 헤더 토큰을 확인하세요."),
    SUCCESS_LOGOUT(HttpStatus.OK, "성공적으로 로그아웃했습니다."),

    /**
     * Member
     * */
    SUCCESS_BUSINESS_VERIFY_CODE(HttpStatus.OK, "성공적으로 인증 코드가 생성되었습니다."),
    SUCCESS_BUSINESS_VERIFY(HttpStatus.OK, "성공적으로 인증되었습니다"),
    SUCCESS_BUSINESS_SETTINGS(HttpStatus.OK, "성공적으로 프로필 세팅을 완료했습니다."),
    /**
     * Admin
     * */
    SUCCESS_BUSINESS_APPROVED(HttpStatus.OK, "성공적으로 비즈니스 멤버를 승인하였습니다."),
    SUCCESS_BUSINESS_APPROVAL_LIST_RETRIEVED(HttpStatus.OK, "성공적으로 승인받을 비즈니스 멤버 리스트를 불러왔습니다."),

    /**
     * home
     * */
    SUCCESS_GET_MY_KEYWORD(HttpStatus.OK, "내가 선택한 키워드를 조회했습니다."),
    SUCCESS_GET_RECOMMEND_WITH_FAVORITE(HttpStatus.OK, "나의 취향에 맞는 베뉴 5개를 불러왔습니다."),
    SUCCESS_GET_RECOMMEND_WITH_FAVORITE_AND_FILTER(HttpStatus.OK, "선호도와 필터 조건에 맞는 베뉴 5개를 불러왔습니다."),


    /**
     * Magazine
     * */
    SUCCESS_CREATED_MAGAZINE(HttpStatus.CREATED, "매거진이 성공적으로 작성되었습니다."),
    SUCCESS_GET_MAGAZINE_LIST(HttpStatus.OK, "매거진이 성공적으로 불러왔습니다."),



    /**
     * empty
     * */
    SUCCESS_BUT_EMPTY_LIST(HttpStatus.OK,"성공적으로 조회했으나 리스트가 비었습니다."),


    /**
     * scrap
     * */



    ;
    private final HttpStatus httpStatus;
    private final String message;

    @Override
    public HttpStatus getStatus() {
        return this.httpStatus;
    }

    @Override
    public String getMessage() {
        return this.message;
    }
}