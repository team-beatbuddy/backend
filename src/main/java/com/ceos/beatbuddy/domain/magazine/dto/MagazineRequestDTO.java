package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Getter
public class MagazineRequestDTO {
    @Schema(description = "제목")
    private String title;
    @Schema(description = "내용")
    private String content;

    public static Magazine toEntity(MagazineRequestDTO dto, Member member, List<String> imageUrls) {
        return Magazine.builder()
                .likes(0)
                .views(0)
                .scraps(null)
                .isVisible(true)
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrls(imageUrls)
                .member(member)
                .build();
    }
}
