package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MagazineHomeResponseDTO {
    private Long magazineId;
    private String thumbImageUrl;

    public static MagazineHomeResponseDTO toDTO(Magazine magazine) {
        return MagazineHomeResponseDTO.builder()
                .magazineId(magazine.getId())
                .thumbImageUrl(magazine.getThumbImage())
                .build();

    }
}
