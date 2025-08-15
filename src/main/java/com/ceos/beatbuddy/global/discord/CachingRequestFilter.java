package com.ceos.beatbuddy.global.discord;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

public class CachingRequestFilter extends OncePerRequestFilter {

    public static final int MAX_BODY_PREVIEW = 2000;
    private static final int CACHE_LIMIT_BYTES = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES);

        // ✅ query string/headers/ip 는 "wrapped"에 기록해야 이후 단계에서 보인다
        String query = wrapped.getQueryString();
        // 디코딩
        if (query != null && !query.isBlank()) {
            query = java.net.URLDecoder.decode(query, "UTF-8");
            wrapped.setAttribute("req.query", query);
        }

        String ua = wrapped.getHeader("User-Agent");
        if (ua != null) wrapped.setAttribute("req.userAgent", ua);

        String xff = wrapped.getHeader("X-Forwarded-For");
        String clientIp = (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : request.getRemoteAddr();
        wrapped.setAttribute("req.clientIp", clientIp);

        chain.doFilter(wrapped, response); // 바디 안 읽고 그냥 진행
    }
}
