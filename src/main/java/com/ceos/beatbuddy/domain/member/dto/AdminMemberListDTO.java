package com.ceos.beatbuddy.domain.member.dto;

import com.ceos.beatbuddy.domain.member.constant.Region;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class AdminMemberListDTO {
    private Long memberId;
    private String loginId;
    private String nickname;
    private String realName;
    private String gender;
    private List<String> regions;

    private String role;

    private LocalDateTime nicknameChangedAt;

    private int nicknameChangeCount;

    private Boolean setNewNickname;
    private Boolean isLocationConsent;
    private Boolean isMarketingConsent;

    private Long latestArchiveId;

    private String profileImage;
    private String fcmToken;
    private String businessName;
    private boolean isApproved;
    private String phoneNumber;
    private LocalDate dateOfBirth;

    private boolean isVerified;

    public static AdminMemberListDTO fromMember(Member member) {
        // 필드 입력
        return AdminMemberListDTO.builder()
                .memberId(member.getId())
                .loginId(member.getLoginId())
                .nickname(member.getNickname())
                .realName(member.getRealName())
                .profileImage(member.getProfileImage())
                .gender(member.getGender() != null ? member.getGender().getText() : null)
                .regions(member.getRegions() != null ? member.getRegions().stream().map(Region::getKorText).toList() : null)
                .role(member.getRole() != null ? member.getRole().toString() : null)
                .nicknameChangedAt(member.getNicknameChangedAt())
                .nicknameChangeCount(member.getNicknameChangeCount())
                .setNewNickname(member.getSetNewNickname())
                .isLocationConsent(member.getIsLocationConsent())
                .isMarketingConsent(member.getIsMarketingConsent())
                .latestArchiveId(member.getLatestArchiveId())
                .fcmToken(member.getFcmToken())
                .businessName(member.getBusinessInfo().getBusinessName() != null ? member.getBusinessInfo().getBusinessName() : null)
                .isApproved(member.getBusinessInfo().isApproved())
                .phoneNumber(member.getBusinessInfo().getPhoneNumber() != null ? member.getBusinessInfo().getPhoneNumber() : null)
                .dateOfBirth(member.getBusinessInfo().getDateOfBirth() != null ? member.getBusinessInfo().getDateOfBirth() : null)
                .isVerified(member.getBusinessInfo().isVerified())
                .build();
    }
}
