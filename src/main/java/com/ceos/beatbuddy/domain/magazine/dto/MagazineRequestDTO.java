package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.exception.MagazineErrorCode;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MagazineRequestDTO {
    @Schema(description = "제목")
    private String title;
    @Schema(description = "내용")
    private String content;
    private boolean isPicked; // 픽된 매거진인지 여부
    private boolean isSponsored; // 스폰서 매거진인지 여부
    private boolean isPinned; // 고정된 매거진인지 여부
    private boolean isVisible; // 띄워줄 매거진인지 여부
    private int orderInHome; // 홈에서의 순서
    private Long eventId;

    public static Magazine toEntity(MagazineRequestDTO dto, Member member) {
        return Magazine.builder()
                .likes(0)
                .views(0)
                .isVisible(dto.isVisible())
                .isPinned(dto.isPinned())
                .isSponsored(dto.isSponsored())
                .isPicked(dto.isPicked())
                .orderInHome(dto.isPinned() ? dto.getOrderInHome() : 0) // isPinned가 true일 때만 orderInHome이 필요
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .build();
    }

    public void validatePinnedWithOrder() {
        if (isPinned && orderInHome <= 0) {
            throw new CustomException(MagazineErrorCode.INVALID_ORDER_IN_HOME);
        }
    }
}
