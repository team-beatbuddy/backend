package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
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
    private LocalDate createAt;
    private List<String> hashtags;

    public static PostPageResponseDTO toDTO(Post post, Boolean liked, Boolean scrapped, Boolean hasCommented, List<FixedHashtag> hashtags) {
        return PostPageResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbImage(post.getImageUrls().isEmpty() ? null : post.getImageUrls().get(0))
                .nickname(post.getMember().getNickname())
                .createAt(post.getCreatedAt().toLocalDate())
                .likes(post.getLikes())
                .scraps(post.getScraps().size())
                .comments(post.getComments())
                .liked(liked)
                .scrapped(scrapped)
                .hasCommented(hasCommented)
                .role(post.getMember().getRole().toString())
                .hashtags(hashtags != null ? hashtags.stream()
                        .map(FixedHashtag::getDisplayName)
                        .toList() : List.of())
                .build();
    }
}
