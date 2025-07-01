package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class MagazineDetailDTO {
    private Long magazineId;
    private String title;
    private String content;
    private Long writerId;
    private List<String> imageUrls;
    private Integer views;
    private Integer likes;
    private LocalDateTime createdAt;

    public static MagazineDetailDTO toDTO(Magazine magazine) {
        return MagazineDetailDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .imageUrls(magazine.getImageUrls())
                .writerId(magazine.getMember().getId())
                .likes(magazine.getLikes())
                .views(magazine.getViews())
                .build();
    }

    public static MagazineDetailDTO toResponseDTO(Magazine magazine) {
        return MagazineDetailDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .createdAt(magazine.getCreatedAt())
                .imageUrls(magazine.getImageUrls())
                .writerId(magazine.getMember().getId())
                .build();
    }
}
