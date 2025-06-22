package com.ceos.beatbuddy.domain.member.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BusinessMemberDTO {
    @Schema(description = "실명", example = "홍길동")
    @NotNull(message = "실명은 필수입니다.")
    private String realName;

    @Schema(description = "핸드폰 번호", example = "010-1234-5678")
    @NotNull(message = "핸드폰 번호는 필수입니다.")
    private String phoneNumber;

    @Schema(description = "통신사", example = "SKT")
    @NotNull(message = "통신사는 필수입니다.")
    private String telCarrier;

    @Schema(description = "주민등록번호 앞 7자리", example = "9001011")
    @NotNull(message = "주민번호 앞 7자리는 필수입니다.")
    private String residentRegistration;
}
