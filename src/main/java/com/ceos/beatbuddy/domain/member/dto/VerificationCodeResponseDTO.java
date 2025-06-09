package com.ceos.beatbuddy.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationCodeResponseDTO {
    private String code;

}
