package com.ceos.beatbuddy.domain.search.exception;

import com.ceos.beatbuddy.global.ApiCode;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum SearchErrorCode implements ApiCode {

    KEYWORD_IS_EMPTY(HttpStatus.BAD_REQUEST,"검색어가 입력되지 않았습니다." ),
    SORT_CRITERIA_EMPTY(HttpStatus.BAD_REQUEST,"정렬 기준이 입력되지 않았습니다." ),
    UNAVAILABLE_SORT_CRITERIA(HttpStatus.BAD_REQUEST,"'가까운 순', '거리순' 또는 '인기순'만 입력해주세요." ),
    COORDINATES_REQUIRED_FOR_DISTANCE_SORT(HttpStatus.BAD_REQUEST, "거리 기반 정렬('가까운 순', '거리순')을 위해서는 위도와 경도 정보가 필요합니다."),
    INVALID_LATITUDE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 위도값입니다. (33.0 ~ 43.0 범위)"),
    INVALID_LONGITUDE_RANGE(HttpStatus.BAD_REQUEST, "유효하지 않은 경도값입니다. (124.0 ~ 132.0 범위)");

    private final HttpStatus httpStatus;
    private final String message;


    SearchErrorCode(HttpStatus httpStatus, String message) {
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
