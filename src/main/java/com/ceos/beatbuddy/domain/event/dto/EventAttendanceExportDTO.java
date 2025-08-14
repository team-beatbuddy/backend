package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventAttendanceExportDTO {
    private String name;
    private String gender;
    private String phoneNumber;
    private String isPaid;
    private String totalMember;
    private String snsType;
    private String snsId;
    private LocalDateTime localDateTime;

    public static EventAttendanceExportDTO toDTO(EventAttendance entity) {
        return EventAttendanceExportDTO.builder()
                .name(entity.getName() != null ? entity.getName() : "-")
                .gender(entity.getGender().getText() != null ? entity.getGender().getText() : "-")
                .phoneNumber(entity.getPhoneNumber() != null ? entity.getPhoneNumber() : "-")
                .build();
    }

    public static EventAttendanceExportDTO toDTOForExcel(EventAttendance entity) {
        return EventAttendanceExportDTO.builder()
                .localDateTime(entity.getCreatedAt())
                .name(entity.getName() != null ? entity.getName() : "-")
                .gender(entity.getGender().getText() != null? entity.getGender().getText() : "-")
                .phoneNumber(entity.getPhoneNumber() != null ? entity.getPhoneNumber() : "-")
                .snsType(entity.getSnsType() != null ? entity.getSnsType().name() : "-")
                .snsId(entity.getSnsId() != null ? entity.getSnsId() : "-")
                .isPaid(entity.getHasPaid() != null ? entity.getHasPaid().toString() : "-")
                .totalMember(entity.getTotalMember() != null? entity.getTotalMember().toString() : "-")
                .build();
    }
}
