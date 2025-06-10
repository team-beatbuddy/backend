package com.ceos.beatbuddy.domain.member.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessMemberResponseDTO {
    private String realName;
    private String phoneNumber;
    private String role;

    public static BusinessMemberResponseDTO toDTO(Member member) {
        return BusinessMemberResponseDTO.builder()
                .realName(member.getRealName())
                .phoneNumber(member.getPhoneNumber())
                .role(member.getRole())
                .build();
    }
}
