package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PostPageResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbImage;
    private String role;
    private int likes;
    private int scraps;
    private int comments;
    private boolean liked;
    private boolean scrapped;
    private boolean hasCommented;
    private String nickname;
    private LocalDateTime createAt;
    private List<String> hashtags;

    @JsonProperty("isAuthor")
    private Boolean isAuthor;
    private Long writerId;
    private String profileImageUrl;

    @JsonProperty("isAnonymous")
    private Boolean isAnonymous;

    public Boolean getIsAuthor() {
        return isAuthor;
    }
    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public static PostPageResponseDTO toDTO(Post post, Boolean liked, Boolean scrapped, Boolean hasCommented, List<FixedHashtag> hashtags, boolean isAuthor) {
        return PostPageResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbImage(post.getImageUrls().isEmpty() || post.getImageUrls().get(0) == null ? "" : post.getImageUrls().get(0))
                .nickname(post.getMember().getNickname())
                .createAt(post.getCreatedAt())
                .likes(post.getLikes())
                .scraps(post.getScraps())
                .comments(post.getComments())
                .liked(liked)
                .scrapped(scrapped)
                .hasCommented(hasCommented)
                .role(post.getMember().getRole().toString())
                .hashtags(hashtags != null ? hashtags.stream()
                        .map(FixedHashtag::getDisplayName)
                        .toList() : List.of())
                .isAuthor(isAuthor)
                .writerId(post.getMember().getId())
                .isAnonymous(post.isAnonymous())
                .profileImageUrl(post.getMember().getProfileImage() != null ? post.getMember().getProfileImage() : "")
                .build();
    }
}
