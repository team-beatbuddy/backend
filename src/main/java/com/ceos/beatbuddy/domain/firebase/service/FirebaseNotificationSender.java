package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import com.ceos.beatbuddy.domain.firebase.repository.FailedNotificationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationSender implements NotificationSender {
    private final ObjectMapper objectMapper;
    private final FailedNotificationRepository failedNotificationRepository;
    private static final int MAX_RETRY_COUNT = 3;
    @Override
    public void send(String targetToken, NotificationPayload payload) {
        if (targetToken == null || targetToken.trim().isEmpty()) {
            log.warn("❌ FCM 토큰 없음: 전송 생략");
            saveFailedNotification(targetToken, payload, "토큰 없음");
            return;
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
            saveFailedNotification(targetToken, payload, e.getMessage());
        }
    }

    private void saveFailedNotification(String token, NotificationPayload payload, String reason) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);

            Optional<FailedNotification> optional = failedNotificationRepository.findByTargetTokenAndPayloadJson(token, payloadJson);

            if (optional.isPresent()) {
                FailedNotification existing = optional.get();

                if (existing.getRetryCount() >= MAX_RETRY_COUNT) {
                    log.warn("🚫 재시도 한도 초과: retryCount={}, token={}", existing.getRetryCount(), token);
                    return; // 재시도 횟수 초과 시 저장 생략
                }

                existing.setRetryCount(existing.getRetryCount() + 1);
                existing.setLastTriedAt(LocalDateTime.now());
                existing.setFailReason(reason);

                failedNotificationRepository.save(existing);
                log.info("🔁 실패 알림 업데이트: retryCount={}, reason={}", existing.getRetryCount(), reason);

            } else {
                FailedNotification failed = FailedNotification.toEntity(token, payloadJson, reason);
                failed.setRetryCount(1);
                failed.setLastTriedAt(LocalDateTime.now());

                failedNotificationRepository.save(failed);
                log.info("🆕 새로운 실패 알림 저장: reason={}", reason);
            }

        } catch (JsonProcessingException e) {
            log.error("❌ payload 직렬화 실패: {}", e.getMessage());
        }
    }

}
