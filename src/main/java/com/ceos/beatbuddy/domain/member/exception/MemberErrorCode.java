package com.ceos.beatbuddy.domain.member.exception;

import com.ceos.beatbuddy.domain.member.dto.error.MemberErrorCodeResponse;
import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum MemberErrorCode implements ApiCode {

    NICKNAME_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    LOGINID_ALREADY_EXIST(HttpStatus.CONFLICT, "이미 존재하는 로그인 ID입니다."),
    INVALID_MEMBER_INFO(HttpStatus.BAD_REQUEST, "잘못된 회원정보입니다."),
    INVALID_PASSWORD_INFO(HttpStatus.BAD_REQUEST, "잘못된 비밀번호입니다."),
    MEMBER_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 회원입니다"),
    REGION_NOT_EXIST(HttpStatus.NOT_FOUND, "존재하지 않는 지역입니다."),
    REGION_FIELD_EMPTY(HttpStatus.NOT_FOUND, "관심 지역이 설정되어 있지 않습니다."),
    UNAVAILABLE_REGION(HttpStatus.NOT_FOUND, "사용자가 관심지역으로 선택했던 지역 리스트에 해당되지 않는 지역입니다."),
    NICKNAME_OVER_LENGTH(HttpStatus.NOT_FOUND, "닉네임이 12자 초과입니다"),
    NICKNAME_SPACE_EXIST(HttpStatus.NOT_FOUND, "닉네임에 공백이 있습니다"),
    NICKNAME_SYMBOL_EXIST(HttpStatus.NOT_FOUND, "닉네임에 특수문자가 있습니다"),
    USERNAME_NOT_MATCH(HttpStatus.BAD_REQUEST, "인증한 성명과 유저의 이름이 일치하지 않습니다."),
    MEMBER_NOT_ADULT(HttpStatus.BAD_REQUEST, "인증한 유저는 성인이 아닙니다."),
    NOT_ADMIN(HttpStatus.BAD_REQUEST, "해당 계정은 어드민이 아닙니다."),
    INVALID_VERIFICATION_CODE(HttpStatus.BAD_REQUEST, "인증번호가 올바르지 않습니다."),
    TEMPORARY_MEMBER_INFO_NOT_FOUND(HttpStatus.NOT_FOUND, "임시저장된 정보를 찾을 수 없습니다."),
    VERIFICATION_CODE_EXPIRED(HttpStatus.EXPECTATION_FAILED, "인증번호가 만료되었습니다. 다시 생성해주세요."),
    SAME_NICKNAME(HttpStatus.BAD_REQUEST, "동일한 닉네임으로는 변경이 불가능합니다."),
    NICKNAME_CHANGE_LIMITED(HttpStatus.BAD_REQUEST, "닉네임 변경은 14일 내에 2번까지만 가능합니다. 14일 뒤에 변경해주세요."),
    NICKNAME_CONFLICT(HttpStatus.CONFLICT, "동일한 시점에 닉네임 변경 시도가 발생했습니다. 잠시 후 다시 시도해주세요."),
    
    // Member Blocking Error Codes
    CANNOT_BLOCK_SELF(HttpStatus.BAD_REQUEST, "자기 자신을 차단할 수 없습니다."),
    ALREADY_BLOCKED(HttpStatus.CONFLICT, "이미 차단된 사용자입니다."),
    BLOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "차단 관계를 찾을 수 없습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String message;


    MemberErrorCode(HttpStatus httpStatus, String message) {
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

