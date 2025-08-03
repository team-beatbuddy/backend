package com.ceos.beatbuddy.global.util;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UploadResult {
    private final String originalUrl;
    private final String thumbnailUrl; // null일 수 있음
}