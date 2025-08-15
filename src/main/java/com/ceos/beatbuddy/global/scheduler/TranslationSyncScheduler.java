package com.ceos.beatbuddy.global.scheduler;

import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 번역 동기화 스케줄러
 * application.yml에서 scheduler.translation.enabled=true로 설정하면 활성화됩니다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(name = "scheduler.translation.enabled", havingValue = "true", matchIfMissing = false)
public class TranslationSyncScheduler {

    private final VenueInfoService venueInfoService;

    /**
     * 매일 새벽 2시에 번역 동기화 실행
     * cron: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "${scheduler.translation.cron:0 0 2 * * ?}")
    @Async
    public void syncVenueTranslations() {
        log.info("스케줄된 번역 동기화 작업을 시작합니다.");
        
        try {
            venueInfoService.syncVenueTranslations();
            log.info("스케줄된 번역 동기화 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("스케줄된 번역 동기화 작업 중 오류 발생", e);
        }
    }
}