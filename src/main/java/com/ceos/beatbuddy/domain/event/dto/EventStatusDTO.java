package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatusDTO {
    private Long eventId;
    private String title;
    private EventStatus status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime currentTime;
    private String statusDescription;
    
    public static EventStatusDTO from(Event event) {
        LocalDateTime now = LocalDateTime.now();
        String description = getStatusDescription(event, now);
        
        return EventStatusDTO.builder()
                .eventId(event.getId())
                .title(event.getTitle())
                .status(event.getStatus())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .currentTime(now)
                .statusDescription(description)
                .build();
    }
    
    private static String getStatusDescription(Event event, LocalDateTime now) {
        // 실제 조건에 따른 올바른 상태 계산
        boolean shouldBeUpcoming = event.getStartDate().isAfter(now); // startDate > 오늘
        boolean shouldBeNow = !event.getStartDate().isAfter(now) && !event.getEndDate().isBefore(now); // startDate <= 오늘 && endDate >= 오늘
        boolean shouldBePast = event.getEndDate().isBefore(now); // endDate < 오늘
        
        switch (event.getStatus()) {
            case UPCOMING:
                if (shouldBePast) {
                    return "⚠️ UPCOMING이지만 이미 종료됨 (endDate < 오늘) - PAST로 변경 필요";
                } else if (shouldBeNow) {
                    return "⚠️ UPCOMING이지만 진행 중 (startDate <= 오늘) - NOW로 변경 필요";
                } else if (shouldBeUpcoming) {
                    return "✅ 예정된 이벤트 (정상) - startDate > 오늘";
                }
                return "❓ UPCOMING 상태 분석 불가";
                
            case NOW:
                if (shouldBePast) {
                    return "⚠️ NOW이지만 이미 종료됨 (endDate < 오늘) - PAST로 변경 필요";
                } else if (shouldBeUpcoming) {
                    return "⚠️ NOW이지만 아직 시작 전 (startDate > 오늘) - UPCOMING으로 변경 필요";
                } else if (shouldBeNow) {
                    return "✅ 진행 중인 이벤트 (정상) - startDate <= 오늘 && endDate >= 오늘";
                }
                return "❓ NOW 상태 분석 불가";
                
            case PAST:
                if (shouldBeUpcoming) {
                    return "⚠️ PAST이지만 아직 시작 전 (startDate > 오늘) - UPCOMING으로 변경 필요";
                } else if (shouldBeNow) {
                    return "⚠️ PAST이지만 진행 중 (endDate >= 오늘) - NOW로 변경 필요";
                } else if (shouldBePast) {
                    return "✅ 종료된 이벤트 (정상) - endDate < 오늘";
                }
                return "❓ PAST 상태 분석 불가";
                
            default:
                return "❓ 알 수 없는 상태";
        }
    }
}