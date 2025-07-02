package com.ceos.beatbuddy.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class NicknameDTO {
    /**
     * 닉네임
     */
    @NotNull(message = "닉네임은 필수 입력값입니다.")
    private String nickname;
}
