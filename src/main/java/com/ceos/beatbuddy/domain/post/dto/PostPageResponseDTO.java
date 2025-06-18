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
    private String nickname;
    private LocalDate createAt;

    public static PostPageResponseDTO toDTO(Post post) {
        return PostPageResponseDTO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .thumbImage(post.getImageUrls().isEmpty() ? null : post.getImageUrls().get(0))
                .comments(post.getComments())
                .nickname(post.getMember().getNickname())
                .createAt(post.getCreatedAt().toLocalDate())
                .likes(post.getLikes())
                .role(post.getMember().getRole())
                .scraps(post.getScraps().size())
                .build();
    }
}
