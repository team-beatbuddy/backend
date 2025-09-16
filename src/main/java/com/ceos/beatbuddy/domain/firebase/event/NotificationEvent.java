package com.ceos.beatbuddy.domain.firebase.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotificationEvent {
    private String targetToken;
    private String title;
    private String body;
    private String imageUrl;
    private Map<String, String> data;

    /**
     * FCM 토큰이 유효한지 확인
     */
    public boolean hasValidToken() {
        return targetToken != null && !targetToken.trim().isEmpty();
    }

    /**
     * 알림 내용이 유효한지 확인
     */
    public boolean hasValidContent() {
        return title != null && !title.trim().isEmpty();
    }
}