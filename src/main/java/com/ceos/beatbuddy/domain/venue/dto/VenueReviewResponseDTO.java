package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VenueReviewResponseDTO {
    @Schema(description = "장소 리뷰 ID", example = "1")
    private Long venueReviewId;
    @Schema(description = "장소 리뷰 내용", example = "이 장소는 정말 멋져요!")
    private String content; // 리뷰 내용
    @Schema(description = "작성자 닉네임", example = "john_doe")
    private String nickname;
    @Schema(description = "좋아요 수", example = "10")
    private int likes; // 좋아요 수
    @Schema(description = "좋아요 여부", example = "true")
    private boolean liked; // 좋아요 눌렀는지 여부
    @Schema(description = "작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl; // 프로필 이미지 URL
    @Schema(description = "사용자 역할", example = "USER")
    private String role; // 사용자 역할 (예: "USER", "ADMIN")
    @Schema(description = "리뷰 작성 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime createdAt; // 리뷰 작성 시간
    @Schema(description = "리뷰 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> imageUrls;
    @Schema(description = "리뷰 썸네일 이미지 URL 목록", example = "[\"https://example.com/thumb1.jpg\", \"https://example.com/thumb2.jpg\"]")
    private List<String> thumbImageUrls;

    @Schema(description = "작성자가 본인인지 여부", example = "true")
    @JsonProperty("isAuthor")
    private Boolean isAuthor; // 작성자가 본인인지 여부
    @Schema(description = "작성자 ID", example = "1")
    private Long writerId; // 작성자 ID
    @Schema(description = "팔로우 여부", example = "true")
    @JsonProperty("isFollowing")
    private Boolean isFollowing;

    public Boolean getIsAuthor() {
        return isAuthor;
    }
    public Boolean getIsFollowing() {return isFollowing;}

    public static VenueReviewResponseDTO toDTO(VenueReview entity, boolean liked, boolean isAuthor, boolean isFollowing) {
        return VenueReviewResponseDTO.builder()
                .venueReviewId(entity.getId())
                .content(entity.getContent())
                .nickname(entity.getMember().getNickname())
                .likes(entity.getLikes())
                .liked(liked)
                .profileImageUrl(entity.getMember().getProfileImage())
                .role(entity.getMember().getRole().name())
                .createdAt(entity.getCreatedAt())
                .imageUrls(entity.getImageUrls() != null ? entity.getImageUrls() : List.of())
                .thumbImageUrls(entity.getThumbnailUrls() != null ? entity.getThumbnailUrls() : List.of())
                .isAuthor(isAuthor) // 작성자가 본인인지 여부
                .writerId(entity.getMember().getId())
                .isFollowing(isFollowing) // 팔로우 여부
                .build();
    }
}
