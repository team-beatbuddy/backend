package com.ceos.beatbuddy.domain.firebase.scheduler;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import com.ceos.beatbuddy.domain.firebase.entity.FirebaseMessageType;
import com.ceos.beatbuddy.domain.firebase.entity.Notification;
import com.ceos.beatbuddy.domain.firebase.repository.FailedNotificationRepository;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.firebase.service.NotificationService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseMessageScheduler {
    private final FailedNotificationRepository failedNotificationRepository;
    private final NotificationSender notificationSender;
    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;
    private final EventAttendanceRepository eventAttendanceRepository;
    private final NotificationPayloadFactory notificationPayloadFactory;
    private final StringRedisTemplate redisTemplate;
    private final NotificationService notificationService;


    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void retryFailedNotifications() {
        String cacheKey = "no_failed_notifications";

        if (Boolean.TRUE.equals(redisTemplate.hasKey(cacheKey))) {
            log.info("재시도 대상 없음 캐시 hit. 쿼리 생략");
            return;
        }

        List<FailedNotification> toRetry = failedNotificationRepository
                .findTop100ByResolvedFalseAndRetryCountLessThanOrderByLastTriedAtAsc(3);

        if (toRetry.isEmpty()) {
            // ❄캐시 저장: 10분간 유지
            redisTemplate.opsForValue().set(cacheKey, "true", Duration.ofMinutes(10));
            log.info("실패 알림 없음 → 캐시 저장 (10분)");
            return;
        }

        toRetry.forEach(failed -> {
            try {
                NotificationPayload payload = objectMapper.readValue(failed.getPayloadJson(), NotificationPayload.class);
                notificationSender.send(failed.getTargetToken(), payload); // 재시도

                failed.setResolved(true); // 성공했으면 resolved 처리
            } catch (Exception e) {
                failed.setRetryCount(failed.getRetryCount() + 1);
                failed.setLastTriedAt(LocalDateTime.now());
            }

            failedNotificationRepository.save(failed);
        });

        // 쿼리 결과 있었으니 캐시 삭제
        redisTemplate.delete(cacheKey);
    }


    // 참여하기로 한 이벤트 리마인드 알림
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void remindEventAttendance() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = from.plusDays(1);

        List<Member> members = memberRepository.findAllByFcmTokenIsNotNull();

        for (Member member : members) {
            List<Event> events = eventAttendanceRepository
                    .findEventsByMemberAndStartDateBetween(member, from, to);

            for (Event event : events) {
                // 1. 먼저 빈 payload로 Notification 저장
                NotificationPayload dummyPayload = NotificationPayload.builder()
                        .title("") // 임시
                        .body("")
                        .data(Map.of("type", FirebaseMessageType.EVENT.getType()))
                        .build();

                Notification saved = notificationService.save(member, dummyPayload);

                // 2. 알림 id 포함된 payload 생성
                NotificationPayload payload = event.getStartDate().toLocalDate().equals(today)
                        ? notificationPayloadFactory.createEventAttendanceDDayNotificationPayload(event.getId(), event.getTitle(), saved.getId())
                        : notificationPayloadFactory.createEventAttendanceD1NotificationPayload(event.getId(), event.getTitle(), saved.getId());

                // 3. 실제 저장된 알림 업데이트 (제목/내용 등)
                saved.update(payload.getTitle(), payload.getBody(), payload.getImageUrl());

                // 4. 푸시 전송
                notificationSender.send(member.getFcmToken(), payload);
            }
        }
    }
}
