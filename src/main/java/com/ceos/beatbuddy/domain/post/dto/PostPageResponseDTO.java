package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.Post;
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
public class PostPageResponseDTO {
    private Long id;
    private String title;
    private String content;
    private String thumbImage;
    private String role;
    private Integer likes;
    private Integer scraps;
    private Integer comments;
    private Boolean liked;
    private Boolean scrapped;
    private Boolean hasCommented;
    private String nickname;
    private LocalDate createAt;

    public static PostPageResponseDTO toDTO(Post post, Boolean liked, Boolean scrapped, Boolean hasCommented) {
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
                .role(post.getMember().getRole())
                .build();
    }
}
