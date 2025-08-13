package com.ceos.beatbuddy.domain.event.scheduler;

import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 * * * *", zone ="Asia/Seoul") // 매 정시, 시간 무시해도 안정적으로 정렬됨
    @Transactional
    public void updateEventStatusesSafely() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfToday = today.atStartOfDay();          // 00:00:00
            LocalDateTime endOfToday   = today.atTime(LocalTime.MAX);   // 23:59:59.999...

            log.info("🔄 이벤트 상태 업데이트 시작: today={}, start={}, end={}", today, startOfToday, endOfToday);

            int nowUpdated = eventRepository.updateToNow(startOfToday, endOfToday);
            log.info("📍 UPCOMING -> NOW 업데이트: {}건", nowUpdated);

            int pastUpdated = eventRepository.updateToPast(startOfToday);
            log.info("📍 NOW -> PAST 업데이트: {}건", pastUpdated);

            int directPastUpdated = eventRepository.updateUpcomingToPast(startOfToday);
            log.info("📍 UPCOMING -> PAST 직접 업데이트: {}건", directPastUpdated);

            log.info("✅ 이벤트 상태 업데이트 완료 - 총 {}건 처리",
                    nowUpdated + pastUpdated + directPastUpdated);
        } catch (Exception e) {
            log.error("❌ 이벤트 상태 업데이트 실패", e);
        }
    }

    
    /**
     * 수동 실행용 메서드 (테스트/디버깅용)
     */
    @Transactional
    public void runManually() {
        try {
            LocalDate today = LocalDate.now();
            LocalDateTime startOfToday = today.atStartOfDay();          // 00:00:00
            LocalDateTime endOfToday   = today.atTime(LocalTime.MAX);   // 23:59:59.999...
            log.info("🔧 수동 이벤트 상태 업데이트 실행: {}", today);

            // 1. UPCOMING -> NOW 상태 업데이트
            int nowUpdated = eventRepository.updateToNow(startOfToday, endOfToday);
            log.info("📍 UPCOMING -> NOW 업데이트: {}건", nowUpdated);

            // 2. NOW -> PAST 상태 업데이트  
            int pastUpdated = eventRepository.updateToPast(startOfToday);
            log.info("📍 NOW -> PAST 업데이트: {}건", pastUpdated);

            // 3. UPCOMING -> PAST 직접 업데이트 (종료시간이 지난 UPCOMING 이벤트)
            int directPastUpdated = eventRepository.updateUpcomingToPast(startOfToday);
            log.info("📍 UPCOMING -> PAST 직접 업데이트: {}건", directPastUpdated);

            log.info("✅ 수동 이벤트 상태 업데이트 완료 - 총 {}건 처리", 
                    nowUpdated + pastUpdated + directPastUpdated);
        } catch (Exception e) {
            log.error("❌ 수동 이벤트 상태 업데이트 실패", e);
            throw e; // 예외를 다시 던져서 컨트롤러에서도 알 수 있게 함
        }
    }
}