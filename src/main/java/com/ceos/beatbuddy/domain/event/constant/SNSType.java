package com.ceos.beatbuddy.domain.event.constant;

import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SNSType {
    NONE("없음"),
    INSTAGRAM("인스타그램"),
    FACEBOOK("페이스북");

    private final String displayName;

    public static SNSType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return NONE;
        }
        
        try {
            return SNSType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new CustomException(EventErrorCode.SNS_TYPE_NOT_EXIST);
        }
    }
    
    public boolean isNone() {
        return this == NONE;
    }
}