package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class MagazineResponseDTO {
    private Long magazineId;
    private String title;
    private String content;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private Long writerId;

    public static MagazineResponseDTO toDTO(Magazine magazine) {
        return MagazineResponseDTO.builder()
                .magazineId(magazine.getId())
                .title(magazine.getTitle())
                .content(magazine.getContent())
                .createdAt(magazine.getCreatedAt())
                .imageUrls(magazine.getImageUrls())
                .writerId(magazine.getMember().getId())
                .build();
    }
}
