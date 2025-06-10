package com.ceos.beatbuddy.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data // 또는 @Getter + @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyPhoneNumberDTO {
    @Schema(description = "핸드폰 번호", example = "010-0000-0000")
    @NotNull(message = "핸드폰 번호는 필수입니다.")
    private String phoneNumber;
}
