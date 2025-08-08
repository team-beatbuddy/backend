package com.ceos.beatbuddy.domain.event.scheduler;

import com.ceos.beatbuddy.domain.event.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventStatusScheduler {

    private final EventRepository eventRepository;

    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void updateEventStatusesSafely() {
        try {
            LocalDateTime now = LocalDateTime.now();
            log.info("🔄 이벤트 상태 업데이트 시작: {}", now);

            // 1. UPCOMING -> NOW 상태 업데이트
            int nowUpdated = eventRepository.updateToNow(now);
            log.info("📍 UPCOMING -> NOW 업데이트: {}건", nowUpdated);

            // 2. NOW -> PAST 상태 업데이트  
            int pastUpdated = eventRepository.updateToPast(now);
            log.info("📍 NOW -> PAST 업데이트: {}건", pastUpdated);

            // 3. UPCOMING -> PAST 직접 업데이트 (종료시간이 지난 UPCOMING 이벤트)
            int directPastUpdated = eventRepository.updateUpcomingToPast(now);
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
            LocalDateTime now = LocalDateTime.now();
            log.info("🔧 수동 이벤트 상태 업데이트 실행: {}", now);

            // 1. UPCOMING -> NOW 상태 업데이트
            int nowUpdated = eventRepository.updateToNow(now);
            log.info("📍 UPCOMING -> NOW 업데이트: {}건", nowUpdated);

            // 2. NOW -> PAST 상태 업데이트  
            int pastUpdated = eventRepository.updateToPast(now);
            log.info("📍 NOW -> PAST 업데이트: {}건", pastUpdated);

            // 3. UPCOMING -> PAST 직접 업데이트 (종료시간이 지난 UPCOMING 이벤트)
            int directPastUpdated = eventRepository.updateUpcomingToPast(now);
            log.info("📍 UPCOMING -> PAST 직접 업데이트: {}건", directPastUpdated);

            log.info("✅ 수동 이벤트 상태 업데이트 완료 - 총 {}건 처리", 
                    nowUpdated + pastUpdated + directPastUpdated);
        } catch (Exception e) {
            log.error("❌ 수동 이벤트 상태 업데이트 실패", e);
            throw e; // 예외를 다시 던져서 컨트롤러에서도 알 수 있게 함
        }
    }
}