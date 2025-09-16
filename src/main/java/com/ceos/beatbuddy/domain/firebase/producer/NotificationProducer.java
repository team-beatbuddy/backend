package com.ceos.beatbuddy.domain.firebase.producer;

import com.ceos.beatbuddy.domain.firebase.event.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "notification-events";

    public void sendNotificationEvent(NotificationEvent event) {
        if (!event.hasValidToken()) {
            log.warn("❌ FCM 토큰 없음: Kafka 전송 생략");
            return;
        }

        if (!event.hasValidContent()) {
            log.warn("❌ 알림 내용 없음: Kafka 전송 생략");
            return;
        }

        try {
            String eventJson = objectMapper.writeValueAsString(event);

            CompletableFuture<SendResult<String, String>> future =
                kafkaTemplate.send(TOPIC, event.getTargetToken(), eventJson);

            future.whenComplete((result, throwable) -> {
                if (throwable != null) {
                    log.error("❌ Kafka 전송 실패 (token: {}): {}",
                        event.getTargetToken(), throwable.getMessage());
                } else {
                    log.info("✅ Kafka 전송 성공 (token: {}, topic: {}, partition: {})",
                        event.getTargetToken(),
                        result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("❌ JSON 직렬화 실패: {}", e.getMessage());
        }
    }
}