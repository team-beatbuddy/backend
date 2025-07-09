package com.ceos.beatbuddy.domain.report.entity;

import com.ceos.beatbuddy.domain.report.exception.ReportErrorCode;
import com.ceos.beatbuddy.global.CustomException;

public enum ReportTargetType {
    FREE_POST,             // 자유게시판 게시글
    PIECE_POST,            // 조각모집 게시글
    EVENT,                 // 이벤트
    VENUE,                 // 베뉴
    FREE_POST_COMMENT,
    EVENT_COMMENT,
    VENUE_COMMENT;

    public static ReportTargetType from(String value) {
        try {
            return ReportTargetType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new CustomException(ReportErrorCode.INVALID_REPORT_TARGET_TYPE);
        }
    }

}