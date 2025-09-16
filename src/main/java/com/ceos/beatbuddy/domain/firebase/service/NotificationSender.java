package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;

public interface NotificationSender {
    void send(String targetToken, NotificationPayload payload);

    /**
     * 동기식 알림 전송 (재전송용)
     * @param targetToken FCM 토큰
     * @param payload 알림 페이로드
     * @return 전송 성공 여부
     */
    boolean sendSync(String targetToken, NotificationPayload payload);
}
