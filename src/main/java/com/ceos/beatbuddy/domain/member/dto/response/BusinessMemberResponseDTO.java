package com.ceos.beatbuddy.domain.member.dto.response;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BusinessMemberResponseDTO {
    private String realName;
    private String phoneNumber;
    private String role;
    private String nickname;

    public static BusinessMemberResponseDTO toDTO(Member member) {
        return BusinessMemberResponseDTO.builder()
                .realName(member.getRealName())
                .phoneNumber(member.getBusinessInfo().getPhoneNumber())
                .role(member.getRole().toString())
                .build();
    }

    public static BusinessMemberResponseDTO toSetNicknameDTO(Member member){
        return BusinessMemberResponseDTO.builder()
                .realName(member.getRealName())
                .phoneNumber(member.getBusinessInfo() != null ? member.getBusinessInfo().getPhoneNumber() : null)
                .role(member.getRole().toString())
                .nickname(member.getNickname())
                .build();
    }
}
