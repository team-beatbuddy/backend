package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.Post;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostReadDetailDTO {
    @JsonUnwrapped
    PostPageResponseDTO postPageResponseDTO;
    private List<String> imageUrls;
    private int views;

    public static PostReadDetailDTO toDTO(Post post, boolean liked, boolean scrapped, boolean commented, List<FixedHashtag> hashtags) {
        return PostReadDetailDTO.builder()
                .postPageResponseDTO(PostPageResponseDTO.toDTO(post, liked, scrapped, commented, hashtags))
                .imageUrls(post.getImageUrls())
                .views(post.getViews())
                .build();
    }
}
