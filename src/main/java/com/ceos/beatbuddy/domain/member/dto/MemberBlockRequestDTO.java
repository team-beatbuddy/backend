package com.ceos.beatbuddy.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberBlockRequestDTO {
    @NotNull(message = "차단할 멤버 ID는 필수입니다.")
    private Long blockedMemberId;
}