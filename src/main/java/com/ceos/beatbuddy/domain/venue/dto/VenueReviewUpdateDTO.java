package com.ceos.beatbuddy.domain.venue.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class VenueReviewUpdateDTO {
    @Size(max = 400, message = "리뷰 내용은 최대 400자까지 입력할 수 있습니다.")
    private String content;
    private List<String> deleteImageUrls;
}
