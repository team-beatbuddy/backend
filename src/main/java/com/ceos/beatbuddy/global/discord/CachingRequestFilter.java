package com.ceos.beatbuddy.global.discord;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

public class CachingRequestFilter extends OncePerRequestFilter {

    public static final int MAX_BODY_PREVIEW = 2000;
    private static final int CACHE_LIMIT_BYTES = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        if (request.getRequestURI().startsWith("/login/oauth2/code/")) {
            chain.doFilter(request, response);
            return;
        }

        String path = request.getRequestURI();

        // ✅ OAuth2 콜백 및 토큰 요청 경로는 건너뜀
        if (path.startsWith("/login/oauth2/code/") || path.startsWith("/oauth2/authorization")) {
            chain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES);

        // query string
        String query = wrapped.getQueryString();
        if (query != null && !query.isBlank()) {
            query = URLDecoder.decode(query, StandardCharsets.UTF_8);
            wrapped.setAttribute("req.query", query);
        }

        // user agent
        String ua = wrapped.getHeader("User-Agent");
        if (ua != null) wrapped.setAttribute("req.userAgent", ua);

        // client ip
        String xff = wrapped.getHeader("X-Forwarded-For");
        String clientIp = (xff != null && !xff.isBlank())
                ? xff.split(",")[0].trim()
                : request.getRemoteAddr();
        wrapped.setAttribute("req.clientIp", clientIp);

        chain.doFilter(wrapped, response);
    }
}
