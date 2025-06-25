package com.ceos.beatbuddy.domain.venue.dto;

import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VenueReviewRequestDTO {
    @NotNull(message = "리뷰 내용은 필수입니다. 400자까지만 가능합니다.")
    @Size(max = 400, message = "리뷰 내용은 400자까지만 입력 가능합니다.")
    private String content; // 리뷰 내용
    private boolean isAnonymous; // 익명 여부

    // member와 venue, 이미지는 VenueReviewRequestDTO에서 직접 다루지 않음
    public static VenueReview toEntity(VenueReviewRequestDTO dto) {
        // VenueReview 엔티티 생성
        return VenueReview.builder()
                .content(dto.getContent())
                .isAnonymous(dto.isAnonymous())
                .likes(0)
                .build();
    }
}
