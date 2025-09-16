package com.ceos.beatbuddy.domain.follow.listener;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.follow.entity.FollowCreatedEvent;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class FollowNotificationListener {

    private final NotificationService notificationService;
    private final MemberRepository memberRepository; // 추가

    @TransactionalEventListener
    public void handleFollowCreated(FollowCreatedEvent event) {
        log.info("🔔 팔로우 알림 시작 - follower: {}, following: {}", event.getFollowerId(), event.getFollowingId());

        try {
            log.info("💾 팔로우 알림 DB 저장 시작");

            Member receiver = memberRepository.findById(event.getFollowingId())
                    .orElseThrow(() -> new IllegalArgumentException("Member not found"));

            Map<String, String> data = new HashMap<>();
            data.put("type", "FOLLOW");
            data.put("followerId", String.valueOf(event.getFollowerId()));

            NotificationPayload payload = NotificationPayload.builder()
                    .title("팔로우 알림")
                    .body(event.getMessage())
                    .data(data)
                    .build();

            notificationService.save(receiver, payload);

            log.info("✅ 팔로우 알림 DB 저장 완료");
        } catch (Exception e) {
            log.error("❌ 팔로우 알림 DB 저장 실패", e);
            throw e;
        }
    }
}