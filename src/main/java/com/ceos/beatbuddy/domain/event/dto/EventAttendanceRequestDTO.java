package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventAttendanceId;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendanceRequestDTO {
    private String name;
    @Schema(description = "성별 (MALE, FEMALE, None)")
    private String gender;
    private String phoneNumber;
    private Integer totalNumber;
    private Boolean isPaid;
    private String snsType;
    private String snsId;

    public static EventAttendance toEntity(EventAttendanceRequestDTO dto, Member member, Event event) {
        EventAttendance.EventAttendanceBuilder builder = EventAttendance.builder()
                .id(new EventAttendanceId(event.getId(), member.getId()))
                .event(event)
                .member(member);

        if (dto.getName() != null) builder.name(dto.getName());
        if (dto.getGender() != null) builder.gender(Gender.fromText(dto.getGender()));
        if (dto.getPhoneNumber() != null) builder.phoneNumber(dto.getPhoneNumber());
        if (dto.getTotalNumber() != null) builder.totalMember(dto.getTotalNumber());
        if (dto.getSnsType() != null) builder.snsType(dto.getSnsType());
        if (dto.getSnsId() != null) builder.snsId(dto.getSnsId());
        builder.hasPaid(dto.getIsPaid());

        return builder.build();
    }

}
