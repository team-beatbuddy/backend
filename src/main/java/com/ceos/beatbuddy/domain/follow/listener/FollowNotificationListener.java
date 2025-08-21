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
            log.info("🔔 팔로우 알림 시작 - follower: {}, following: {}", 
                    event.follower().getId(), event.following().getId());
                    
            NotificationPayload payload = notificationPayloadFactory
                    .createFollowPayload(event.follower().getId(), event.follower().getNickname());

            // DB에 먼저 저장 (무조건 성공)
            log.info("💾 팔로우 알림 DB 저장 시작");
            Notification saved = notificationService.save(event.following(), payload);
            payload.getData().put("notificationId", String.valueOf(saved.getId()));
            log.info("✅ 팔로우 알림 DB 저장 완료 - notificationId: {}", saved.getId());

            // FCM 전송은 별도 처리 (실패해도 DB에는 저장됨)
            try {
                log.info("🚀 팔로우 FCM 전송 시작");
                notificationSender.send(event.following().getFcmToken(), payload);
                log.info("✅ 팔로우 FCM 전송 성공");
            } catch (Exception e) {
                log.warn("⚠️ 팔로우 FCM 전송 실패하지만 알림은 목록에서 확인 가능: {}", e.getMessage());
            }
        } catch (Exception e) {
            log.error("❌ 팔로우 알림 DB 저장 실패", e);
        }
    }
}