package com.ceos.beatbuddy.domain.magazine.dto;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Builder
@Getter
public class MagazineRequestDTO {
    private String title;
    private String content;

    public static Magazine toEntity(MagazineRequestDTO dto, Member member, List<String> imageUrls) {
        return Magazine.builder()
                .likes(0)
                .views(0)
                .reposts(0)
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrls(imageUrls)
                .member(member)
                .build();
    }
}
