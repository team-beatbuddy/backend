package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.domain.venue.kakaoMap.CoordinateResponse;
import com.ceos.beatbuddy.domain.venue.kakaoMap.KakaoLocalClient;
import com.ceos.beatbuddy.domain.venue.repository.VenueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class VenueCoordinateUpdateService {

    private final VenueRepository venueRepository;
    private final KakaoLocalClient kakaoLocalClient;

    @Async
    @Transactional
    public CompletableFuture<Void> updateAllVenueCoordinates() {
        log.info("🚀 베뉴 좌표 업데이트 배치 작업 시작");
        
        List<Venue> venues = venueRepository.findAll();
        int totalCount = venues.size();
        int successCount = 0;
        int failCount = 0;
        
        log.info("📍 총 {} 개의 베뉴 좌표 업데이트 시작", totalCount);
        
        for (int i = 0; i < venues.size(); i++) {
            Venue venue = venues.get(i);
            
            try {
                // 이미 좌표가 있는 경우 스킵
                if (venue.getLatitude() != 0.0 && venue.getLongitude() != 0.0) {
                    log.debug("⏭️ 이미 좌표가 설정됨: {} (위도: {}, 경도: {})", 
                        venue.getKoreanName(), venue.getLatitude(), venue.getLongitude());
                    continue;
                }
                
                // 주소가 없는 경우 스킵
                if (venue.getAddress() == null || venue.getAddress().trim().isEmpty()) {
                    log.warn("⚠️ 주소가 없어 스킵: {} (ID: {})", venue.getKoreanName(), venue.getId());
                    failCount++;
                    continue;
                }
                
                // 카카오 API로 좌표 조회 (비동기 처리)
                kakaoLocalClient
                    .getCoordinateFromAddress(venue.getAddress())
                    .subscribe(
                        coordinate -> {
                            // 좌표 업데이트 (x는 경도, y는 위도)
                            venueRepository.updateLatLng(venue.getId(), coordinate.getY(), coordinate.getX());
                            log.info("✅ [{}/%{}] 좌표 업데이트 완료: {} (위도: {}, 경도: {})", 
                                i + 1, totalCount, venue.getKoreanName(), coordinate.getY(), coordinate.getX());
                        },
                        error -> {
                            log.warn("❌ [{}/%{}] 좌표 조회 실패: {} (주소: {}) - {}", 
                                i + 1, totalCount, venue.getKoreanName(), venue.getAddress(), error.getMessage());
                        }
                    );
                
                // API 호출 제한을 위한 딜레이 (카카오 API는 초당 10건 제한)
                if (i % 10 == 9) {
                    Thread.sleep(1000);
                }
                
            } catch (Exception e) {
                failCount++;
                log.error("💥 [{}/%{}] 베뉴 좌표 업데이트 중 오류 발생: {} (ID: {}) - {}", 
                    i + 1, totalCount, venue.getKoreanName(), venue.getId(), e.getMessage());
            }
        }
        
        log.info("🏁 베뉴 좌표 업데이트 배치 작업 완료 - 성공: {}, 실패: {}, 총: {}", 
            successCount, failCount, totalCount);
            
        return CompletableFuture.completedFuture(null);
    }
    
    @Async
    @Transactional
    public CompletableFuture<Void> updateSpecificVenueCoordinate(Long venueId) {
        log.info("🎯 특정 베뉴 좌표 업데이트 시작: ID {}", venueId);
        
        try {
            Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new IllegalArgumentException("베뉴를 찾을 수 없습니다: " + venueId));
            
            if (venue.getAddress() == null || venue.getAddress().trim().isEmpty()) {
                log.warn("⚠️ 주소가 없어 업데이트 불가: {} (ID: {})", venue.getKoreanName(), venue.getId());
                return CompletableFuture.completedFuture(null);
            }
            
            CoordinateResponse coordinate = kakaoLocalClient
                .getCoordinateFromAddress(venue.getAddress())
                .block();
            
            if (coordinate != null) {
                venueRepository.updateLatLng(venue.getId(), coordinate.getY(), coordinate.getX());
                log.info("✅ 좌표 업데이트 완료: {} (위도: {}, 경도: {})", 
                    venue.getKoreanName(), coordinate.getY(), coordinate.getX());
            } else {
                log.warn("❌ 좌표 조회 실패: {} (주소: {})", venue.getKoreanName(), venue.getAddress());
            }
            
        } catch (Exception e) {
            log.error("💥 베뉴 좌표 업데이트 중 오류 발생: ID {} - {}", venueId, e.getMessage());
        }
        
        return CompletableFuture.completedFuture(null);
    }
    
    @Transactional(readOnly = true)
    public void printVenuesWithoutCoordinates() {
        List<Venue> venues = venueRepository.findAll();
        
        log.info("📊 좌표가 없는 베뉴 목록:");
        venues.stream()
            .filter(venue -> venue.getLatitude() == 0.0 && venue.getLongitude() == 0.0)
            .forEach(venue -> log.info("- {} (ID: {}, 주소: {})", 
                venue.getKoreanName(), venue.getId(), venue.getAddress()));
    }
}