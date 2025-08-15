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

    private static final int MAX_BODY_PREVIEW = 2000;
    private static final int CACHE_LIMIT_BYTES = 10 * 1024 * 1024; // 10MB

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // 래핑 + 캐시 용량 확장
        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, CACHE_LIMIT_BYTES);

        // 1) 체인 들어가기 전, 미리 필요한 정보 다 채워서 attr에 넣어둔다.
        //    (예외가 나더라도 전역 예외 처리기에서 바로 읽을 수 있게)
        // 쿼리 인코딩 param=%E3%85%8E%E3%85%87%E3%85%8E%E3%85%87 인코딩 필요
        String query = Optional.ofNullable(request.getQueryString())
                .map(q -> URLDecoder.decode(q, StandardCharsets.UTF_8))
                .orElse("");
        request.setAttribute("req.query", query);
        String ip = Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(v -> v.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
        request.setAttribute("req.clientIp", ip);
        request.setAttribute("req.userAgent", request.getHeader("User-Agent"));

        // 바디 미리보기: 텍스트/JSON/폼만
        String ct = Optional.ofNullable(request.getContentType()).orElse("");
        boolean isTextLike = ct.contains("json") || ct.startsWith("text/")
                || ct.contains("xml") || ct.contains("form");
        if (isTextLike) {
            try {
                // 한 번 읽어서 캐시에 적재
                StreamUtils.copyToByteArray(wrapped.getInputStream());



                // 캐시에서 문자열로 꺼냄
                String body = new String(wrapped.getContentAsByteArray(), StandardCharsets.UTF_8);
                if (body.length() > MAX_BODY_PREVIEW) {
                    body = body.substring(0, MAX_BODY_PREVIEW) + "…(+truncated)";
                }
                request.setAttribute("req.bodyPreview", body.isBlank() ? "(empty)" : body);
            } catch (Exception ignore) {
                request.setAttribute("req.bodyPreview", "(unreadable)");
            }
        } else {
            request.setAttribute("req.bodyPreview", "(non-text body or empty)");
        }

        // 2) 이제 체인 진행 (이미 캐시/attr 세팅되어 있으므로
        //    예외가 나도 예외 핸들러에서 즉시 읽을 수 있음)
        chain.doFilter(wrapped, response);



    }
}
