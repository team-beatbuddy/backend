package com.ceos.beatbuddy.domain.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmTokenUpdateDTO {
    @NotBlank(message = "FCM 토큰은 필수입니다.")
    private String token;
}