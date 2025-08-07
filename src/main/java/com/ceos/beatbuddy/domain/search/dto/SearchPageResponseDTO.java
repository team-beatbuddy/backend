package com.ceos.beatbuddy.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "검색 결과 페이지 응답 DTO")
public class SearchPageResponseDTO {
    @Schema(description = "페이지 번호", example = "1")
    private int page;
    @Schema(description = "페이지당 알림 개수", example = "10")
    private int size;
    @Schema(description = "총 결과 개수", example = "100")
    private int totalElements;
    @Schema(description = "총 페이지 수", example = "10")
    private int totalPages;
    @Schema(description = "현재 페이지의 결과 리스트")
    private List<SearchQueryResponseDTO> content;
}
