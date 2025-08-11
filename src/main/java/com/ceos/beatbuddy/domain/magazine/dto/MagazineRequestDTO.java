package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class MagazineRequestDTO {
    @Schema(description = "제목")
    private String title;
    @Schema(description = "내용")
    private String content;
    private boolean pinned;
    private boolean picked;
    private boolean sponsored;
    private Boolean visible;
    @Min(value = 1, message = "홈에서의 순서는 1 이상이어야 합니다")
    @Max(value = 5, message = "홈에서의 순서는 5 이하여야 합니다")
    private Integer orderInHome; // 홈에서의 순서
    private Long eventId;
    private List<Long> venueIds;

    public static Magazine toEntity(MagazineRequestDTO dto, Member member) {
        return Magazine.builder()
                .likes(0)
                .views(0)
                .isVisible(dto.getVisible() == null || dto.getVisible())
                .isPinned(dto.isPinned())
                .isSponsored(dto.isSponsored())
                .isPicked(dto.isPicked())
                .orderInHome(dto.isPinned() ? dto.getOrderInHome() : 0)
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .build();
    }
}
