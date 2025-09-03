package com.ceos.beatbuddy.domain.member.constant;

import com.ceos.beatbuddy.domain.event.exception.EventErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Gender {
    TYPE1(0, "MALE"),
    TYPE2(1, "FEMALE"),
    TYPE3(2, "NONE");


    private Integer idx;
    private String text;

    Gender(Integer idx, String text) {
        this.idx = idx;
        this.text = text;
    }

    @JsonCreator
    public static Gender fromText(String text) {
        if (text == null || text.trim().isEmpty()) {
            return TYPE3; // Default to NONE for empty values
        }
        
        for (Gender gender : Gender.values()) {
            if (gender.getText().equalsIgnoreCase(text.trim())) {
                return gender;
            }
        }
        throw new CustomException(EventErrorCode.INVALID_GENDER);
    }
    
    public boolean isNone() {
        return this == TYPE3;
    }

    public Integer getIdx() {
        return idx;
    }

    @JsonValue
    public String getText() {
        return text;
    }

    public static Gender fromIdx(Integer idx) {
        for (Gender gender : Gender.values()) {
            if (gender.getIdx().equals(idx)) {
                return gender;
            }
        }
        return null;
    }
}
