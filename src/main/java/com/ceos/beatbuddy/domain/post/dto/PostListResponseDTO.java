package com.ceos.beatbuddy.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostListResponseDTO {
    private int totalPost;
    private int size;
    private int page;
    private List<PostPageResponseDTO> responseDTOS;
}
