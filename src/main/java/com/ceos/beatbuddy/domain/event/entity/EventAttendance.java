package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceUpdateDTO;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class EventAttendance extends BaseTimeEntity {

    @EmbeddedId
    private EventAttendanceId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("memberId")
    @JoinColumn(name = "memberId")
    @Setter(AccessLevel.PROTECTED)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "eventId")
    @Setter(AccessLevel.PROTECTED)
    private Event event;

    @Setter(AccessLevel.PROTECTED)
    private String name;
    @Setter(AccessLevel.PROTECTED)
    private Gender gender;
    @Setter(AccessLevel.PROTECTED)
    private String phoneNumber;

    @Setter(AccessLevel.PROTECTED)
    private Integer totalMember; // 동행인원 (본인 포함)

    @Setter(AccessLevel.PROTECTED)
    private String snsType; // 인스타그램, 페이스북, X
    @Setter(AccessLevel.PROTECTED)
    private String snsId;

    @Setter(AccessLevel.PROTECTED)
    private Boolean hasPaid;

    public void applyUpdate(EventAttendanceUpdateDTO dto) {
        if (dto.getName() != null) this.setName(dto.getName());
        if (dto.getGender() != null) this.setGender(dto.getGender());
        if (dto.getPhoneNumber() != null) this.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getTotalMember() != null) this.setTotalMember(dto.getTotalMember());
        if (dto.getSnsType() != null) this.setSnsType(dto.getSnsType());
        if (dto.getSnsId() != null) this.setSnsId(dto.getSnsId());
        if (dto.getHasPaid() != null) this.setHasPaid(dto.getHasPaid());
    }
}