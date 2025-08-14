package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.constant.SNSType;
import com.ceos.beatbuddy.domain.member.constant.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventAttendanceUpdateDTO {

    private String name;

    private Gender gender;

    private String phoneNumber;

    private Integer totalMember; // 동행 인원

    private SNSType snsType;

    private String snsId;

    private Boolean hasPaid;
}