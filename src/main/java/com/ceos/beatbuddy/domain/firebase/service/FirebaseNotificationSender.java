package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.global.discord.DiscordNotificationFailureNotifier;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationSender implements NotificationSender {
    private final DiscordNotificationFailureNotifier discordNotifier;
    @Override
    @Async
    public void send(String targetToken, NotificationPayload payload) {
        if (targetToken == null || targetToken.trim().isEmpty()) {
            log.warn("❌ FCM 토큰 없음: 전송 생략 (재시도 대상 아님)");
            return; // ❌ DB 저장도 하지 않음
        }

        try {
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(payload.getTitle())
                            .setBody(payload.getBody())
                            .setImage(payload.getImageUrl())
                            .build())
                    .putAllData(payload.getData())
                    .build();

            String response = FirebaseMessaging.getInstance().send(message);
            log.info("푸시 알림 전송 성공 (token: {}): {}", targetToken, response);

        } catch (FirebaseMessagingException e) {
            log.warn("푸시 알림 전송 실패 (token: {}): {}", targetToken, e.getMessage());

            // 디스코드로 실패 알림
            discordNotifier.sendNotificationFailure(
                targetToken,
                payload.getTitle(),
                payload.getBody(),
                e.getMessage()
            );
        }
    }


    @Override
    public boolean sendSync(String targetToken, NotificationPayload payload) {
        // 재전송 기능 제거로 인해 sendSync는 더 이상 사용하지 않음
        throw new UnsupportedOperationException("sendSync는 더 이상 지원하지 않습니다.");
    }

}
