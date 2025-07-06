package com.ceos.beatbuddy.domain.post.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdatePostRequestDTO {
    private String title;
    private String content;
    private List<String> hashtags;
    private List<String> deleteImageUrls;
}
