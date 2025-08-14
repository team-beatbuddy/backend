package com.ceos.beatbuddy.global.discord;

public record ErrorNotice(
        String env, String method, String path, int status,
        Long memberId, String exception, String reason, String message,
        String traceId, String occurredAt
) {}