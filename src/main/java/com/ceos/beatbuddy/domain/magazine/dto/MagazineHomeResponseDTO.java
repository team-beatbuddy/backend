package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MagazineHomeResponseDTO {
    private Long magazineId;
    private String thumbImageUrl;
    private int orderInHome; // 홈에서의 순서
    private String title;
    private String content;
    private boolean liked;
    private boolean sponsored; // 스폰서 매거진인지 여부
    private boolean picked; // 픽된 매거진인지 여부

    @JsonProperty("isAuthor")
    private Boolean isAuthor;

    public Boolean getIsAuthor() {
        return isAuthor;
    }


    public static MagazineHomeResponseDTO toDTO(Magazine magazine, boolean liked, boolean isAuthor) {
        return MagazineHomeResponseDTO.builder()
                .magazineId(magazine.getId())
                .thumbImageUrl(magazine.getThumbImage() != null ? magazine.getThumbImage() : "")
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .orderInHome(magazine.getOrderInHome())
                .liked(liked)
                .picked(magazine.isPicked())
                .sponsored(magazine.isSponsored())
                .isAuthor(isAuthor)
                .build();

    }
}
