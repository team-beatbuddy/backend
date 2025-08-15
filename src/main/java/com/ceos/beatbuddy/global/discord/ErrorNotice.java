package com.ceos.beatbuddy.global.discord;

import java.util.Map;

public record ErrorNotice(
        String env, String method, String path, int status,
        Long memberId, String exception, String reason, String message,
        String traceId, String occurredAt,
        String queryString,
        String bodyPreview,
        String clientIp,
        String userAgent
) {}