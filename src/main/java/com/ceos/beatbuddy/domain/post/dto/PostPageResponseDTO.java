package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "게시글 페이지 응답 DTO")
public class PostPageResponseDTO {
    @Schema(description = "게시글 ID", example = "1")
    private Long id;
    @Schema(description = "게시글 제목", example = "오늘의 공연 후기")
    private String title;
    @Schema(description = "게시글 내용", example = "오늘 공연 너무 좋았어요!")
    private String content;
    @Schema(description = "썸네일 이미지 URL 목록", example = "[\"https://example.com/image1.jpg\", \"https://example.com/image2.jpg\"]")
    private List<String> thumbImage;
    @Schema(description = "게시글 작성자 역할", example = "USER")
    private String role;
    @Schema(description = "게시글 좋아요 수", example = "100")
    private int likes;
    @Schema(description = "게시글 스크랩 수", example = "50")
    private int scraps;
    @Schema(description = "게시글 댓글 수", example = "20")
    private int comments;
    @Schema(description = "게시글 좋아요 여부", example = "true")
    private boolean liked;
    @Schema(description = "게시글 스크랩 여부", example = "false")
    private boolean scrapped;
    @Schema(description = "게시글 댓글 작성 여부", example = "true")
    private boolean hasCommented;
    @Schema(description = "게시글 작성자 닉네임", example = "요시")
    private String nickname;
    @Schema(description = "게시글 작성 날짜", example = "2023-10-01T12:00:00")
    private LocalDateTime createAt;
    @Schema(description = "게시글 해시태그 목록", example = "[\"홍대\", \"이태원\"]")
    private List<String> hashtags;

    @JsonProperty("isAuthor")
    @Schema(description = "게시글 작성자가 현재 사용자와 동일한지 여부", example = "true")
    private Boolean isAuthor;
    @Schema(description = "게시글 작성자 ID", example = "1")
    private Long writerId;
    @Schema(description = "게시글 작성자 프로필 이미지 URL", example = "https://example.com/profile.jpg")
    private String profileImageUrl;

    @JsonProperty("isAnonymous")
    @Schema(description = "게시글이 익명 게시글인지 여부", example = "false")
    private Boolean isAnonymous;

    @JsonProperty("isFollowing")
    @Schema(description = "게시글 작성자를 팔로우하고 있는지 여부", example = "true")
    private Boolean isFollowing;

    public Boolean getIsFollowing() { return isFollowing; }
    public Boolean getIsAuthor() {
        return isAuthor;
    }
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public static PostPageResponseDTO toDTO(Post post, Boolean liked, Boolean scrapped, Boolean hasCommented, List<FixedHashtag> hashtags, boolean isAuthor, boolean isFollowing) {
        // FreePost인 경우 해시태그를 직접 가져옴
        List<FixedHashtag> actualHashtags = (post instanceof FreePost) ? 
            ((FreePost) post).getHashtag() : hashtags;
        return PostPageResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbImage(post.getThumbnailUrls() != null? post.getThumbnailUrls() : List.of())
                .nickname(post.isAnonymous() 
                        ? "익명" 
                        : (post.getMember().getPostProfileInfo() != null && post.getMember().getPostProfileInfo().getPostProfileNickname() != null
                                ? post.getMember().getPostProfileInfo().getPostProfileNickname()
                                : post.getMember().getNickname()))
                .createAt(post.getCreatedAt())
                .likes(post.getLikes())
                .scraps(post.getScraps())
                .comments(post.getComments())
                .liked(liked)
                .scrapped(scrapped)
                .hasCommented(hasCommented)
                .role(post.getMember().getRole().toString())
                .hashtags(actualHashtags != null ? actualHashtags.stream()
                        .map(FixedHashtag::getDisplayName)
                        .toList() : List.of())
                .isAuthor(isAuthor)
                .writerId(post.getMember().getId())
                .isAnonymous(post.isAnonymous())
                .profileImageUrl(
                        post.isAnonymous()
                                ? ""
                                : (post.getMember().getPostProfileInfo() != null && post.getMember().getPostProfileInfo().getPostProfileImageUrl() != null
                                ? post.getMember().getPostProfileInfo().getPostProfileImageUrl()
                                : (post.getMember().getProfileImage() != null
                                        ? post.getMember().getProfileImage()
                                        : ""))
                )
                .isFollowing(isFollowing)
                .build();
    }
}
