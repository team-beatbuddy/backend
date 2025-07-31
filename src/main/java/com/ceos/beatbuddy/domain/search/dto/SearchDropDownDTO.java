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
        장르 필터 값 (예: '이디엠' 또는 'EDM')
        - 이디엠, EDM
        - 힙합, HIPHOP
        - 알앤비, R&B
        - 하우스, HOUSE
        - 소울, 펑크, 소울앤펑크, SOUL&FUNK
        - 테크노, TECHNO
        - 케이팝, K-POP
        - 팝, POP
        - 라틴, LATIN
        - 락, ROCK
        """)
    private String genreTag;
    @Schema(description = """
        지역 필터 값 (예: '홍대' 또는 'HONGDAE')
        - 홍대, HONGDAE
        - 이태원, ITAEWON
        - 압구정, APGUJEONG
        - 강남_신사, GANGNAM/SINSA
        - 기타, OTHERS
        """)
    private String regionTag;
    private String sortCriteria;
}
