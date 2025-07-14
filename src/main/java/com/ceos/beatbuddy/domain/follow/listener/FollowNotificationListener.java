package com.ceos.beatbuddy.domain.follow.listener;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.follow.entity.FollowCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowNotificationListener {

    private final NotificationService notificationService;
    private final NotificationSender notificationSender;
    private final NotificationPayloadFactory notificationPayloadFactory;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleFollowCreated(FollowCreatedEvent event) {
        try {
            NotificationPayload payload = notificationPayloadFactory
                    .createFollowPayload(event.follower().getId(), event.follower().getNickname());

            Notification saved = notificationService.save(event.following(), payload);
            payload.getData().put("notificationId", String.valueOf(saved.getId()));

            notificationSender.send(event.following().getFcmToken(), payload);
        } catch (Exception e) {
            log.error("❌ 팔로우 알림 전송 실패", e);
            // TODO: 실패 큐 처리 or 단순 로그
        }
    }
}