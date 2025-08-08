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
        switch (event.getStatus()) {
            case UPCOMING:
                if (event.getEndDate().isBefore(now)) {
                    return "⚠️ UPCOMING이지만 종료시간이 지남 - 스케줄러 실행 필요";
                } else if (!event.getStartDate().isAfter(now)) {
                    return "⚠️ UPCOMING이지만 시작시간이 지남 - NOW로 변경 필요";
                }
                return "✅ 예정된 이벤트 (정상)";
            case NOW:
                if (event.getEndDate().isBefore(now)) {
                    return "⚠️ NOW이지만 종료시간이 지남 - PAST로 변경 필요";
                } else if (event.getStartDate().isAfter(now)) {
                    return "⚠️ NOW이지만 아직 시작시간 전 - UPCOMING으로 변경 필요";
                }
                return "✅ 진행 중인 이벤트 (정상)";
            case PAST:
                if (!event.getEndDate().isBefore(now)) {
                    return "⚠️ PAST이지만 아직 종료시간 전 - 상태 변경 필요";
                }
                return "✅ 종료된 이벤트 (정상)";
            default:
                return "❓ 알 수 없는 상태";
        }
    }
}