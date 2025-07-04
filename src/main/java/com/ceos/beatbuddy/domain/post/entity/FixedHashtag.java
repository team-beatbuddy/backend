package com.ceos.beatbuddy.domain.post.entity;

import com.ceos.beatbuddy.domain.post.exception.PostErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Arrays;

public enum FixedHashtag {
    압구정로데오,
    홍대,
    이태원,
    강남_신사,
    뮤직,
    자유,
    번개_모임,
    International,
    NINETEEN_PLUS,
    LGBTQ,
    짤_밈;

    @JsonValue
    public String getDisplayName() {
        return switch (this) {
            case 강남_신사 -> "강남.신사";
            case 번개_모임 -> "번개 모임";
            case NINETEEN_PLUS -> "19+";
            case 짤_밈 -> "짤.밈";
            default -> this.name();
        };
    }

    @JsonCreator
    public static FixedHashtag fromDisplayName(String value) {
        return Arrays.stream(FixedHashtag.values())
                .filter(tag -> tag.getDisplayName().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new CustomException(PostErrorCode.NOT_FOUND_HASHTAG));
    }
}