package com.ceos.beatbuddy.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberProfileSummaryDTO {
    private Long memberId;
    private String nickname;
    private String profileImageUrl;
    private String role;
    private Integer postCount;
    private Integer followerCount;
    private Integer followingCount;

    public static MemberProfileSummaryDTO toDTO(Long id, String nickname, String profileImageUrl, String role, Integer postCount, Integer followerCount, Integer followingCount) {
        return MemberProfileSummaryDTO.builder()
                .memberId(id)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(role)
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .build();
    }
}
