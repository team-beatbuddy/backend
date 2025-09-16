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
import jakarta.annotation.PostConstruct;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    private final ObjectMapper objectMapper;
    private final DiscordNotificationFailureNotifier discordNotifier;

    @PostConstruct
    public void init() {
        log.info("🚀 NotificationConsumer 초기화 완료 - Kafka 리스너 준비됨");
    }


    @KafkaListener(topics = "notification-events", groupId = "notification-group")
    public void consumeNotificationEvent(
        @Payload String message,
        @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
        @Header(KafkaHeaders.OFFSET) long offset,
        Acknowledgment acknowledgment) {

        log.info("🔔 Kafka 알림 이벤트 수신 - topic: {}, partition: {}, offset: {}", topic, partition, offset);

        try {
            NotificationEvent event = objectMapper.readValue(message, NotificationEvent.class);
            log.info("📋 알림 이벤트 파싱 완료 - title: {}, targetToken: {}", event.getTitle(),
                event.getTargetToken() != null ? event.getTargetToken().substring(0, Math.min(20, event.getTargetToken().length())) + "..." : "null");

            // NotificationEvent를 NotificationPayload로 변환
            NotificationPayload payload = NotificationPayload.builder()
                .title(event.getTitle())
                .body(event.getBody())
                .imageUrl(event.getImageUrl())
                .data(event.getData())
                .build();

            // FCM 직접 전송 (순환 참조 방지)
            log.info("📤 sendToFirebase 메서드 호출 시작 - title: {}", event.getTitle());
            sendToFirebase(event.getTargetToken(), payload);
            log.info("📤 sendToFirebase 메서드 호출 완료 - title: {}", event.getTitle());

            // 수동 커밋
            acknowledgment.acknowledge();
            log.info("✅ 알림 이벤트 처리 완료 - title: {}", event.getTitle());

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 역직렬화 실패: {}", e.getMessage());
            // 파싱 실패한 경우에도 커밋하여 무한 재시도 방지
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("❌ 알림 처리 실패: {}", e.getMessage(), e);
            // 처리 실패 시에도 커밋 (재시도 로직은 별도 토픽으로 분리 가능)
            acknowledgment.acknowledge();
        }
    }

    private void sendToFirebase(String targetToken, NotificationPayload payload) {
        log.info("🚀 sendToFirebase 메서드 진입 - title: {}", payload.getTitle());

        if (targetToken == null || targetToken.trim().isEmpty()) {
            log.warn("❌ FCM 토큰이 비어있음 - Firebase 전송 건너뛰기");
            return;
        }

        log.info("🚀 Firebase로 FCM 전송 시작 - title: {}, token: {}...",
            payload.getTitle(),
            targetToken.substring(0, Math.min(20, targetToken.length())));

        try {
            // Firebase 앱 상태 확인
            if (FirebaseApp.getApps().isEmpty()) {
                log.error("❌ Firebase 앱이 초기화되지 않음");
                return;
            }

            log.info("🔧 Firebase 메시지 빌드 시작");
            Message message = Message.builder()
                    .setToken(targetToken)
                    .setNotification(Notification.builder()
                            .setTitle(payload.getTitle())
                            .setBody(payload.getBody())
                            .setImage(payload.getImageUrl())
                            .build())
                    .putAllData(payload.getData())
                    .build();
            log.info("🔧 Firebase 메시지 빌드 완료");

            log.info("📡 FirebaseMessaging.getInstance().send() 호출 시작");
            String messageId = FirebaseMessaging.getInstance().send(message);
            log.info("✅ FCM 전송 성공 - messageId: {}, title: {}", messageId, payload.getTitle());

        } catch (FirebaseMessagingException e) {
            log.error("❌ FCM 전송 실패 - title: {}, error: {}, errorCode: {}",
                payload.getTitle(), e.getMessage(), e.getErrorCode(), e);

            // 디스코드로 실패 알림
            discordNotifier.sendNotificationFailure(
                targetToken,
                payload.getTitle(),
                payload.getBody(),
                e.getMessage()
            );
        } catch (Exception e) {
            log.error("❌ FCM 전송 중 예상치 못한 오류 - title: {}, error: {}",
                payload.getTitle(), e.getMessage(), e);
        }

        log.info("🚀 sendToFirebase 메서드 종료 - title: {}", payload.getTitle());
    }
}