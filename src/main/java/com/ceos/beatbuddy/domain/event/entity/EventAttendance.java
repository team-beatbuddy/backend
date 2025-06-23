package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.event.dto.EventAttendanceUpdateDTO;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.Optional;

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
        Optional.ofNullable(dto.getName()).ifPresent(this::setName);
        Optional.ofNullable(dto.getGender()).ifPresent(this::setGender);
        Optional.ofNullable(dto.getPhoneNumber()).ifPresent(this::setPhoneNumber);
        Optional.ofNullable(dto.getTotalMember()).ifPresent(this::setTotalMember);
        Optional.ofNullable(dto.getSnsType()).ifPresent(this::setSnsType);
        Optional.ofNullable(dto.getSnsId()).ifPresent(this::setSnsId);
        Optional.ofNullable(dto.getHasPaid()).ifPresent(this::setHasPaid);
    }
}