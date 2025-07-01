package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
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

    public static Magazine toEntity(MagazineRequestDTO dto, Member member) {
        return Magazine.builder()
                .likes(0)
                .views(0)
                .isVisible(true)
                .isPinned(false)
                .title(dto.getTitle())
                .content(dto.getContent())
                .member(member)
                .build();
    }
}
