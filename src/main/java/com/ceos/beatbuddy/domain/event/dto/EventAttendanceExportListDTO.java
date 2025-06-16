package com.ceos.beatbuddy.domain.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class EventAttendanceExportListDTO {
    private Long eventId;
    private Integer totalMember;
    private List<EventAttendanceExportDTO> eventAttendanceExportDTOS;
}
