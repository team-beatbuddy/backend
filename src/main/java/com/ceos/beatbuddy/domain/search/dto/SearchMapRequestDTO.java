package com.ceos.beatbuddy.domain.search.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지도 베뉴 검색 요청 DTO")
public class SearchMapRequestDTO {
    @Schema(description = "검색 키워드", example = "홍대")
    private String keyword;
    
    @Schema(description = "지역 필터 값", allowableValues = {"홍대", "압구정", "강남/신사", "이태원", "기타"})
    private String regionTag;
    
    @Schema(description = "장르 필터 값", allowableValues = {"HIPHOP", "R&B", "EDM", "HOUSE", "TECHNO", "SOUL&FUNK", "ROCK", "LATIN", "K-POP", "POP"})
    private String genreTag;
    
    @Schema(description = "정렬 기준", allowableValues = {"인기순", "가까운 순"})
    private String sortCriteria;
    
    @Schema(description = "위도", example = "37.7225216")
    private Double latitude;
    
    @Schema(description = "경도", example = "126.7499008")
    private Double longitude;
    
    @Schema(description = "페이지 번호", example = "1")
    @Builder.Default
    private Integer page = 1;
    
    @Schema(description = "페이지 크기", example = "10")
    @Builder.Default
    private Integer size = 10;
    
    public SearchDropDownDTO toSearchDropDownDTO() {
        return SearchDropDownDTO.builder()
                .keyword(keyword)
                .regionTag(regionTag)
                .genreTag(genreTag)
                .sortCriteria(sortCriteria)
                .build();
    }
}