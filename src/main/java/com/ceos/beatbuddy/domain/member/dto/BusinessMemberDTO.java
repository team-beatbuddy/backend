package com.ceos.beatbuddy.domain.member.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class BusinessMemberDTO {
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    @NotNull(message = "멤버 id 는 필수입니다.")
    private Long memberId;
=======
=======
>>>>>>> Stashed changes
    @Schema(description = "실명", example = "홍길동")
>>>>>>> Stashed changes
    @NotNull(message = "실명은 필수입니다.")
    private String realName;
    @NotNull(message = "핸드폰 번호는 필수입니다.")
    private String phoneNumber;
    @NotNull(message = "통신사는 필수입니다.")
    private String telCarrier;
    @NotNull(message = "주민번호 앞 7자리는 필수입니다.")
    private String residentRegistration;
<<<<<<< Updated upstream
<<<<<<< Updated upstream
    @NotNull(message = "인증번호는 필수입니다.")
    private String verificationCode;
=======
>>>>>>> Stashed changes
=======
>>>>>>> Stashed changes
}
