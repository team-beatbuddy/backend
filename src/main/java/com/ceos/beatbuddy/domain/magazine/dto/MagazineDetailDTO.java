package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MagazineDetailDTO {
    private Long magazineId;
    private String title;
    private String content;
    private Long memberId;
    private List<String> imageUrls;
    private Integer scraps;
    private Integer views;
    private Integer likes;

    public static MagazineDetailDTO toDTO(Magazine magazine) {
        return MagazineDetailDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .imageUrls(magazine.getImageUrls())
                .memberId(magazine.getMember().getId())
                .likes(magazine.getLikes())
                .views(magazine.getViews())
                .scraps(magazine.getScraps().size())
                .build();
    }
}
