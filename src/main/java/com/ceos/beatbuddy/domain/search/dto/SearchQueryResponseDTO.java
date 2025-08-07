package com.ceos.beatbuddy.domain.search.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@Schema(description = "검색 쿼리 응답 DTO")
public class SearchQueryResponseDTO {
    @Schema(description = "현재 날짜 및 시간", example = "2023-10-01T12:00:00")
    private LocalDateTime currentDate;
    @Schema(description = "베뉴 ID", example = "1")
    private Long venueId;
    @Schema(description = "영어 이름", example = "BeatBuddy Venue")
    private String englishName;
    @Schema(description = "한국어 이름", example = "비트버디 베뉴")
    private String koreanName;
    @Schema(description = "태그 리스트", example = "[\"음악\", \"공연\", \"문화\"]")
    private List<String> tagList;
    @Schema(description = "하트비트 수", example = "100")
    private Long heartbeatNum;
    @Schema(description = "하트비트 여부", example = "true")
    private Boolean isHeartbeat;
    @Schema(description = "로고 URL", example = "https://example.com/logo.png")
    private String logoUrl;
    @Schema(description = "배경 이미지 URL 리스트", example = "[\"https://example.com/bg1.png\", \"https://example.com/bg2.png\"]")
    private List<String> backgroundUrl;
    @Schema(description = "주소", example = "서울특별시 강남구 역삼동 123-45")
    private String address;
    @Schema(description = "위도", example = "37.5665")
    private Double latitude;
    @Schema(description = "경도", example = "126.9789")
    private Double longitude;

    @QueryProjection
    public SearchQueryResponseDTO(LocalDateTime currentDate, Long venueId, String englishName, String koreanName, List<String> tagList, Long heartbeatNum, boolean isHeartbeat, String logoUrl, List<String> backgroundUrl, String address, Double latitude, Double longitude) {
        this.currentDate = currentDate;
        this.venueId = venueId;
        this.englishName = englishName;
        this.koreanName = koreanName;
        this.tagList = tagList;
        this.heartbeatNum = heartbeatNum;
        this.isHeartbeat = isHeartbeat;
        this.logoUrl = logoUrl;
        this.backgroundUrl = backgroundUrl;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
