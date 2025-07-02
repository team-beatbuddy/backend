package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Builder
@Getter
public class MagazineDetailDTO {
    private Long magazineId;
    private String title;
    private String content;
    private Long writerId;
    private List<String> imageUrls;
    private int views;
    private int likes;
    private LocalDateTime createdAt;
    private boolean isLiked; // 좋아요 여부

    public static MagazineDetailDTO toDTO(Magazine magazine, boolean isLiked) {
        return MagazineDetailDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .imageUrls(magazine.getImageUrls() != null ? magazine.getImageUrls() : Collections.emptyList())
                .writerId(magazine.getMember().getId())
                .likes(magazine.getLikes())
                .views(magazine.getViews())
                .createdAt(magazine.getCreatedAt())
                .isLiked(isLiked)
                .build();
    }
}
