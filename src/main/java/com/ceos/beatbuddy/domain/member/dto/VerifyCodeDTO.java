package com.ceos.beatbuddy.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // 또는 @Getter + @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VerifyCodeDTO {
    @Schema(description = "인증번호", example = "123456")
    @NotNull(message = "인증번호는 필수입니다.")
    private String code;
}
