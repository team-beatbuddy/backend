package com.ceos.beatbuddy.domain.recent_search.entity;

import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;

import java.util.Arrays;

public enum SearchTypeEnum {
    EVENT, HOME, MAP, FREE_POST;

    public static SearchTypeEnum from(String type) {
        return Arrays.stream(values())
                .filter(e -> e.name().equalsIgnoreCase(type))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_SEARCH_TYPE));
    }
}
