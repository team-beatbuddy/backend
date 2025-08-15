package com.ceos.beatbuddy.global.discord;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class CachingRequestFilter extends OncePerRequestFilter {

    public static final int MAX_BODY_PREVIEW = 2000;
    private static final int CACHE_LIMIT_BYTES = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES);
        chain.doFilter(wrappedRequest, response); // 바디 안 읽고 그냥 진행
    }
}
