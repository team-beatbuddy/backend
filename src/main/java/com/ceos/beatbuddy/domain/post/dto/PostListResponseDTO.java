package com.ceos.beatbuddy.domain.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "게시글 목록 응답 DTO")
public class PostListResponseDTO {
    @Schema(description = "총 게시글 수", example = "100")
    private int totalPost;
    @Schema(description = "페이지 크기", example = "10")
    private int size;
    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;
    @Schema(description = "게시글 응답 DTO 목록")
    private List<PostPageResponseDTO> responseDTOS;
}
