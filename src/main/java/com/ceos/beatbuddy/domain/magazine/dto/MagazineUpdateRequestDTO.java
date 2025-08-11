package com.ceos.beatbuddy.domain.magazine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class MagazineUpdateRequestDTO {
    private String title;
    private String content;

    private Boolean visible;
    private Boolean pinned;
    private Boolean sponsored;
    private Boolean picked;

    private Integer orderInHome;

    private Long eventId; // 연동할 Event ID
    private List<Long> venueIds; // venue ID 리스트

    private List<String> deleteImageUrls; // 삭제할 기존 이미지 URL
    // 이미지는 MultipartFile로 Controller에서 받음
}
