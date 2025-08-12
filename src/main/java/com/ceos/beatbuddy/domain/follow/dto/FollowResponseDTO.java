package com.ceos.beatbuddy.domain.follow.dto;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "팔로우 정보 DTO")
public class FollowResponseDTO {
    @Schema(description = "사용자 ID", example = "1")
    private Long memberId;
    
    @Schema(description = "사용자 닉네임", example = "홍길동")
    private String nickname;
    
    @Schema(description = "사용자 프로필 이미지", example = "https://example.com/profile.jpg")
    private String profileImage;
    
    @Schema(description = "게시판용 닉네임", example = "익명사용자1")
    private String postProfileNickname;
    
    @Schema(description = "게시판용 프로필 이미지", example = "https://example.com/post_profile.jpg")
    private String postProfileImageUrl;

    @JsonProperty("isFollowing")
    private boolean isFollowing; // 팔로우 여부
    
    // isFollowing 설정을 위한 setter
    public void setFollowing(boolean following) {
        this.isFollowing = following;
    }

    // 팔로잉 목록 조회용 - 내가 팔로우하는 사람의 정보
    public static FollowResponseDTO fromFollowingMember(Follow follow) {
        Member followingMember = follow.getFollowing();
        return FollowResponseDTO.builder()
                .memberId(followingMember.getId())
                .nickname(followingMember.getNickname())
                .profileImage(followingMember.getProfileImage())
                .postProfileNickname(followingMember.getPostProfileInfo().getPostProfileNickname())
                .postProfileImageUrl(followingMember.getPostProfileInfo().getPostProfileImageUrl())
                .build();
    }

    // 팔로워 목록 조회용 - 나를 팔로우하는 사람의 정보
    public static FollowResponseDTO fromFollowerMember(Follow follow) {
        Member followerMember = follow.getFollower();
        return FollowResponseDTO.builder()
                .memberId(followerMember.getId())
                .nickname(followerMember.getNickname())
                .profileImage(followerMember.getProfileImage())
                .postProfileNickname(followerMember.getPostProfileInfo().getPostProfileNickname())
                .postProfileImageUrl(followerMember.getPostProfileInfo().getPostProfileImageUrl())
                .build();
    }
    
    /**
     * @deprecated Use {@link #fromFollowingMember(Follow)} or {@link #fromFollowerMember(Follow)} instead.
     * This method is deprecated since version 1.0 and will be removed in a future release.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    public static FollowResponseDTO toDTO(Follow follow) {
        return fromFollowingMember(follow);
    }
}
