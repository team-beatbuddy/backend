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

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebaseNotificationSender implements NotificationSender {
    private final ObjectMapper objectMapper;
    private final FailedNotificationRepository failedNotificationRepository;

    @Override
    public void send(String targetToken, NotificationPayload payload) {
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

            // 실패한 푸시 저장
            try {
                String payloadJson = objectMapper.writeValueAsString(payload);
                FailedNotification failed = FailedNotification.toEntity(targetToken, payloadJson);
                failedNotificationRepository.save(failed);
            } catch (JsonProcessingException ex) {
                log.error("payload 직렬화 실패: {}", ex.getMessage());
            }
        }
    }
}
