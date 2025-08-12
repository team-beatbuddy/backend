package com.ceos.beatbuddy.domain.member.dto.response;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MemberResponseDTO {
    private Long memberId;
    private String loginId;
    private String realName;
    private String nickname;
    private boolean isLocationConsent;
    private boolean isMarketingConsent;

    public static MemberResponseDTO toDTO(Member member) {
        return MemberResponseDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .realName(member.getRealName())
                .nickname(member.getNickname())
                .isLocationConsent(member.getIsLocationConsent())
                .isMarketingConsent(member.getIsMarketingConsent())
                .build();
    }

    public static MemberResponseDTO toSetNicknameDTO(Member member) {
        return MemberResponseDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .build();
    }
}
