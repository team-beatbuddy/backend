package com.ceos.beatbuddy.domain.firebase.scheduler;

import com.ceos.beatbuddy.domain.firebase.NotificationPayload;
import com.ceos.beatbuddy.domain.firebase.entity.FailedNotification;
import com.ceos.beatbuddy.domain.firebase.repository.FailedNotificationRepository;
import com.ceos.beatbuddy.domain.firebase.service.NotificationSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class FirebaseMessageScheduler {
    private final FailedNotificationRepository failedNotificationRepository;
    private final NotificationSender notificationSender;
    private final ObjectMapper objectMapper;

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
}
