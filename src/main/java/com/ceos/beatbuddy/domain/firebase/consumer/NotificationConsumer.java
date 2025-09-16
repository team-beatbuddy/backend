package com.ceos.beatbuddy.domain.firebase.consumer;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.event.NotificationEvent;
import com.ceos.beatbuddy.global.discord.DiscordNotificationFailureNotifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final ObjectMapper objectMapper;
    private final DiscordNotificationFailureNotifier discordNotifier;

    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void consumeNotificationEvent(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("📥 Kafka 메시지 수신 (topic: {}, partition: {}, offset: {})",
            topic, partition, offset);

        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);

            // NotificationEvent를 NotificationPayload로 변환
            NotificationPayload payload = NotificationPayload.builder()
                .title(event.getTitle())
                .body(event.getBody())
                .imageUrl(event.getImageUrl())
                .data(event.getData())
                .build();

            // FCM 직접 전송 (순환 참조 방지)
            sendToFirebase(event.getTargetToken(), payload);

            // 수동 커밋
            acknowledgment.acknowledge();

            log.info("✅ 알림 처리 완료 (token: {})", event.getTargetToken());

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 역직렬화 실패: {}", e.getMessage());
            // 파싱 실패한 경우에도 커밋하여 무한 재시도 방지
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ 알림 처리 실패: {}", e.getMessage());
            // 처리 실패 시에도 커밋 (재시도 로직은 별도 토픽으로 분리 가능)
            acknowledgment.acknowledge();
        }
    }

    private void sendToFirebase(String targetToken, NotificationPayload payload) {
        if (targetToken == null || targetToken.trim().isEmpty()) {
            log.warn("❌ FCM 토큰 없음: 전송 생략");
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
            log.info("✅ FCM 전송 성공 (token: {}): {}", targetToken, response);

        } catch (FirebaseMessagingException e) {
            log.warn("❌ FCM 전송 실패 (token: {}): {}", targetToken, e.getMessage());

            // 디스코드로 실패 알림
            discordNotifier.sendNotificationFailure(
                targetToken,
                payload.getTitle(),
                payload.getBody(),
                e.getMessage()
            );
        }
    }
}