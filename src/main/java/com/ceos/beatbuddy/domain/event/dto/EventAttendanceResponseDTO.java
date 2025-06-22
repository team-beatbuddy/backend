package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendanceResponseDTO {
    private Long eventId;
    private Long memberId;
    private String name;
    private String gender;
    private String snsType;
    private String snsId;
    private String phoneNumber;
    private Boolean isPaid;
    private Integer totalMember;
    private LocalDateTime createdAt;

    public static EventAttendanceResponseDTO toDTO(EventAttendance entity) {
        return EventAttendanceResponseDTO.builder()
                .eventId(entity.getEvent().getId())
                .memberId(entity.getMember().getId())
                .name(entity.getName())
                .gender(entity.getGender() != null ? entity.getGender().getText() : null)
                .phoneNumber(entity.getPhoneNumber())
                .snsType(entity.getSnsType())
                .snsId(entity.getSnsId())
                .isPaid(entity.getHasPaid())
                .totalMember(entity.getTotalMember())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
