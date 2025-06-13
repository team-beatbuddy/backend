package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.scrap.entity.MagazineScrap;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MagazineHomeResponseDTO {
    private Long magazineId;
    private String thumbImageUrl;
    private String title;

    public static MagazineHomeResponseDTO toDTO(Magazine magazine) {
        return MagazineHomeResponseDTO.builder()
                .magazineId(magazine.getId())
                .thumbImageUrl(magazine.getThumbImage())
                .build();

    }

    public static MagazineHomeResponseDTO toScrapDTO(Magazine magazine) {
        return MagazineHomeResponseDTO.builder()
                .magazineId(magazine.getId())
                .thumbImageUrl(magazine.getThumbImage())
                .title(magazine.getTitle())
                .build();
    }
}
