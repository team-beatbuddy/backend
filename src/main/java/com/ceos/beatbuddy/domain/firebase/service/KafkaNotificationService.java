package com.ceos.beatbuddy.domain.firebase.service;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.event.NotificationEvent;
import com.ceos.beatbuddy.domain.firebase.producer.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaNotificationService implements NotificationSender {
    private final NotificationProducer notificationProducer;

    @Override
    public void send(String targetToken, NotificationPayload payload) {
        // NotificationPayload를 NotificationEvent로 변환
        NotificationEvent event = NotificationEvent.builder()
            .targetToken(targetToken)
            .title(payload.getTitle())
            .body(payload.getBody())
            .imageUrl(payload.getImageUrl())
            .data(payload.getData())
            .build();

        // Kafka로 전송
        notificationProducer.sendNotificationEvent(event);
    }

    @Override
    public boolean sendSync(String targetToken, NotificationPayload payload) {
        // Kafka는 비동기 처리이므로 동기 방식은 지원하지 않음
        throw new UnsupportedOperationException("KafkaNotificationService는 동기 전송을 지원하지 않습니다.");
    }
}