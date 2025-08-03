package com.ceos.beatbuddy.domain.search.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SearchDropDownDTO {
    private String keyword;
    @Schema(description = """
        장르 필터 값 (예: 'EDM' 또는 'HOUSE')
        - HIPHOP,R&B,EDM,HOUSE,TECHNO,SOUL&FUNK,ROCK,LATIN,K-POP,POP
        """)
    private String genreTag;
    @Schema(description = """
        지역 필터 값 (예: '홍대', '압구정', '강남/신사', '이태원', '기타')
        """)
    private String regionTag;
    private String sortCriteria;
}
