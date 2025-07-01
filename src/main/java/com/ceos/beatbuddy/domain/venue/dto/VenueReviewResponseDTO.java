package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VenueReviewResponseDTO {
    private Long venueReviewId;
    private String content; // 리뷰 내용
    private String nickname; // 익명이면 "익명"으로 전달
    private int likes; // 좋아요 수
    private boolean liked; // 좋아요 눌렀는지 여부
    private String profileImageUrl; // 프로필 이미지 URL
    private String role; // 사용자 역할 (예: "USER", "ADMIN")
    private LocalDateTime createdAt; // 리뷰 작성 시간
    private List<String> imageUrls;

    public static VenueReviewResponseDTO toDTO(VenueReview entity, boolean liked) {
        return VenueReviewResponseDTO.builder()
                .venueReviewId(entity.getId())
                .content(entity.getContent())
                .nickname(entity.getMember().getNickname())
                .likes(entity.getLikes())
                .liked(liked)
                .profileImageUrl(entity.getMember().getProfileImage())
                .role(entity.getMember().getRole().name())
                .createdAt(entity.getCreatedAt())
                .imageUrls(entity.getImageUrls() != null ? entity.getImageUrls() : List.of())
                .build();
    }
}
