package com.ceos.beatbuddy.global.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.AbstractRequestLoggingFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@Order(1)
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

    public RequestLoggingFilter() {
        setIncludeQueryString(true);
        setIncludePayload(true);
        setMaxPayloadLength(500);
        setIncludeHeaders(false);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // Query parameters 캡처
        String queryString = request.getQueryString();
        if (queryString != null) {
            queryString = new String(queryString.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        }
        request.setAttribute("req.query", queryString);
        
        // Client IP 캡처
        String clientIp = getClientIp(request);
        request.setAttribute("req.clientIp", clientIp);
        
        // User Agent 캡처
        String userAgent = request.getHeader("User-Agent");
        request.setAttribute("req.userAgent", userAgent);
        
        // AbstractRequestLoggingFilter가 자동으로 ContentCachingRequestWrapper로 감싸고 body를 캐시함
        super.doFilterInternal(request, response, filterChain);
        
        // 필터 체인 실행 후 body 캡처
        if (request instanceof ContentCachingRequestWrapper wrappedRequest) {
            String bodyPreview = extractBodyPreview(wrappedRequest);
            request.setAttribute("req.bodyPreview", bodyPreview);
        }
    }

    @Override
    protected void beforeRequest(HttpServletRequest request, String message) {
        // 로깅하지 않음 - Discord만 사용
    }

    @Override
    protected void afterRequest(HttpServletRequest request, String message) {
        // 로깅하지 않음 - Discord만 사용  
    }
    
    
    private String extractBodyPreview(ContentCachingRequestWrapper request) {
        byte[] content = request.getContentAsByteArray();
        if (content.length == 0) {
            return null;
        }
        
        String body = new String(content, StandardCharsets.UTF_8);
        
        // 민감한 정보 마스킹
        body = maskSensitiveData(body);
        
        // Body가 너무 길면 자르기 (최대 500자)
        if (body.length() > 500) {
            body = body.substring(0, 500) + "...";
        }
        
        return body;
    }
    
    private String maskSensitiveData(String body) {
        if (body == null) return null;
        
        // JSON에서 password, token 등 마스킹
        return body.replaceAll("(?i)(\"password\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                  .replaceAll("(?i)(\"token\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                  .replaceAll("(?i)(\"secret\"\\s*:\\s*\")([^\"]*)(\")", "$1***$3")
                  .replaceAll("(?i)(Bearer\\s+)([\\w\\-\\.]+)", "$1***");
    }
    
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        String xRealIp = request.getHeader("X-Real-IP");
        String xForwardedProto = request.getHeader("X-Forwarded-Proto");
        
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        } else if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        } else {
            return request.getRemoteAddr();
        }
    }
}