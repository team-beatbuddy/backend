package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.constant.Gender;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("eventId")
    @JoinColumn(name = "eventId")
    private Event event;

    private String name;

    private Gender gender;
    private String phoneNumber;

    private Integer totalMember; // 동행인원 (본인 포함)

    private String snsType; // 인스타그램, 페이스북, X
    private String snsId;

    private boolean hasPaid;
    //private Integer paymentAmount;
}