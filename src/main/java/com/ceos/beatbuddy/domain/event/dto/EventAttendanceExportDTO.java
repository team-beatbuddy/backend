package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventAttendanceExportDTO {
    private String name;
    private String gender;
    private String phoneNumber;
    private Boolean isPaid;
    private String snsType;
    private String snsId;
    private Integer totalMember;

    public static EventAttendanceExportDTO toDTO(EventAttendance entity) {
        return EventAttendanceExportDTO.builder()
                .name(entity.getName())
                .gender(entity.getGender().getText())
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }

    public static EventAttendanceExportDTO toDTOForExcel(EventAttendance entity) {
        return EventAttendanceExportDTO.builder()
                .name(entity.getName())
                .gender(entity.getGender().getText())
                .phoneNumber(entity.getPhoneNumber())
                .snsType(entity.getSnsType())
                .snsId(entity.getSnsId())
                .isPaid(entity.isHasPaid())
                .totalMember(entity.getTotalMember())
                .build();
    }
}
