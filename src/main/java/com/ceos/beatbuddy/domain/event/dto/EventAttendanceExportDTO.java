package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class EventAttendanceExportDTO {
    private String name;
    private String gender;
    private String phoneNumber;

    public static EventAttendanceExportDTO toDTO(EventAttendance entity) {
        return EventAttendanceExportDTO.builder()
                .name(entity.getName())
                .gender(entity.getGender().getText())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }
}
