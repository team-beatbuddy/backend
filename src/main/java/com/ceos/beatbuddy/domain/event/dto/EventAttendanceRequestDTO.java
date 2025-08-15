package com.ceos.beatbuddy.domain.event.dto;

import com.ceos.beatbuddy.domain.event.constant.SNSType;
import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
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
@Schema(description = "이벤트 참석 요청 DTO")
public class EventAttendanceRequestDTO {
    @Schema(description = "이름", example = "홍길동")
    private String name;
    @Schema(description = "성별 (MALE, FEMALE, None)", example = "MALE", allowableValues = "MALE, FEMALE, NONE")
    private String gender;
    @Schema(description = "전화번호", example = "010-1234-5678")
    private String phoneNumber;
    @Schema(description = "동행인원 (본인 포함)", example = "2")
    private Integer totalNumber;
    @Schema(description = "결제 여부", example = "true")
    private Boolean isPaid;
    @Schema(description = "SNS 타입 (INSTAGRAM, FACEBOOK, NONE 등)", example = "FACEBOOK", allowableValues = "INSTAGRAM, FACEBOOK, NONE")
    private String snsType; // String으로 받아서 enum으로 변환
    @Schema(description = "SNS ID", example = "FACEBOOK123")
    private String snsId;

    public static EventAttendance toEntity(EventAttendanceRequestDTO dto, Member member, Event event) {
        EventAttendance.EventAttendanceBuilder builder = EventAttendance.builder()
                .event(event)
                .member(member);

        if (dto.getName() != null) builder.name(dto.getName());
        if (dto.getGender() != null) builder.gender(Gender.fromText(dto.getGender()));
        if (dto.getPhoneNumber() != null) builder.phoneNumber(dto.getPhoneNumber());
        if (dto.getTotalNumber() != null) builder.totalMember(dto.getTotalNumber());
        
        // String을 SNSType enum으로 변환
        SNSType snsType = SNSType.fromString(dto.getSnsType());
        builder.snsType(snsType);
        
        // SNSType이 NONE인 경우 snsId는 항상 null로 처리
        if (snsType.isNone()) {
            builder.snsId(null);
        } else if (dto.getSnsId() != null) {
            builder.snsId(dto.getSnsId());
        }
        
        builder.hasPaid(dto.getIsPaid());

        return builder.build();
    }

}
