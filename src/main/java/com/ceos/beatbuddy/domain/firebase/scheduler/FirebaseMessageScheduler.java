package com.ceos.beatbuddy.domain.firebase.scheduler;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.repository.EventAttendanceRepository;
import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.NotificationPayloadFactory;
import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import com.ceos.beatbuddy.domain.firebase.repository.FailedNotificationRepository;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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


    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void retryFailedNotifications() {
        List<FailedNotification> toRetry = failedNotificationRepository
                .findTop100ByResolvedFalseAndRetryCountLessThanOrderByLastTriedAtAsc(3);

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
    }

    // 참여하기로 한 이벤트 리마인드 알림
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void remindEventAttendance() {
        LocalDate today = LocalDate.now();
        LocalDateTime from = today.atStartOfDay();
        LocalDateTime to = from.plusDays(1); // 오늘 하루 범위

        List<Member> members = memberRepository.findAllByFcmTokenIsNotNull();

        for (Member member : members) {
            List<Event> events = eventAttendanceRepository
                    .findEventsByMemberAndStartDateBetween(member, from, to);

            for (Event event : events) {
                NotificationPayload payload = event.getStartDate().toLocalDate().equals(today)
                        ? notificationPayloadFactory.createEventAttendanceDDayNotificationPayload(event.getId(), event.getTitle())
                        : notificationPayloadFactory.createEventAttendanceD1NotificationPayload(event.getId(), event.getTitle());

                notificationSender.send(member.getFcmToken(), payload);
            }
        }
    }
}
