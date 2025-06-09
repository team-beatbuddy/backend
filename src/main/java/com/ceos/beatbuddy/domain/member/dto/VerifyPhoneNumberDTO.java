package com.ceos.beatbuddy.domain.member.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class VerifyPhoneNumberDTO {
    @NotNull(message = "핸드폰 번호는 필수입니다.")
    private String phoneNumber;
}
