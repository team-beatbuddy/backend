package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
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
    private String content;
    private boolean liked;

    public static MagazineHomeResponseDTO toDTO(Magazine magazine, boolean liked) {
        return MagazineHomeResponseDTO.builder()
                .magazineId(magazine.getId())
                .thumbImageUrl(magazine.getThumbImage())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .liked(liked)
                .build();

    }
}
