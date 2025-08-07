package com.ceos.beatbuddy.domain.member.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 프로필 요약 정보 DTO")
public class MemberProfileSummaryDTO {
    @Schema(description = "회원 ID", example = "1")
    private Long memberId;
    @Schema(description = "회원 닉네임(게시판용)", example = "홍길동")
    private String nickname;
    @Schema(description = "회원 프로필 이미지 URL(게시판용)", example = "https://example.com/profile.jpg")
    private String profileImageUrl;
    @Schema(description = "회원 역할", example = "USER")
    private String role;
    @Schema(description = "게시글 수", example = "10")
    private Integer postCount;
    @Schema(description = "팔로워 수", example = "100")
    private Integer followerCount;
    @Schema(description = "팔로잉 수", example = "50")
    private Integer followingCount;
    @Schema(description = "회원 사업자명", example = "홍길동의 가게")
    private String businessName;
    // 게시판용 프로필을 만들었는지 여부
    @Schema(description = "게시판용 프로필 생성 여부", example = "true")
    @JsonProperty("isPostProfileCreated")
    private Boolean isPostProfileCreated;

    public static MemberProfileSummaryDTO toDTO(Long id, String nickname, String profileImageUrl, String role, Integer postCount, Integer followerCount, Integer followingCount, String businessName, Boolean isPostProfileCreated) {
        return MemberProfileSummaryDTO.builder()
                .memberId(id)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .role(role)
                .postCount(postCount)
                .followerCount(followerCount)
                .followingCount(followingCount)
                .businessName(businessName)
                .isPostProfileCreated(isPostProfileCreated)
                .build();
    }

    // 기존 호환성을 위한 오버로드 메소드
    public static MemberProfileSummaryDTO toDTO(Long id, String nickname, String profileImageUrl, String role, Integer postCount, Integer followerCount, Integer followingCount) {
        return toDTO(id, nickname, profileImageUrl, role, postCount, followerCount, followingCount, null, false);
    }
}
