package com.ceos.beatbuddy.domain.event.schedular;

import com.ceos.beatbuddy.domain.event.entity.EventStatus;
import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateEventStatusesSafely() {
        try {
            LocalDate today = LocalDate.now();

            eventRepository.updateStatus(EventStatus.NOW, EventStatus.PAST, today.minusDays(1));
            eventRepository.updateStatus(EventStatus.UPCOMING, EventStatus.NOW, today);

            log.info("✅ 이벤트 상태 업데이트 완료");
        } catch (Exception e) {
            log.error("❌ 이벤트 상태 업데이트 실패", e);
        }
    }
}