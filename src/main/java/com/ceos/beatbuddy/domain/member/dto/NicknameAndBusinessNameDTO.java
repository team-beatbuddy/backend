package com.ceos.beatbuddy.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NicknameAndBusinessNameDTO {
    @Schema(description = "비즈니스명", example = "클럽 사운드")
    @NotNull(message = "비즈니스명은 필수입니다.")
    private String businessName;
    @Schema(description = "닉네임", example = "호롤롤")
    @NotNull(message = "닉네임은 필수입니다.")
    private String nickname;
}
