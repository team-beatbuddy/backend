package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class PostCreateRequestDTO {
    @NotNull(message = "제목은 비어있을 수 없습니다.")
    private String title;
    @NotNull(message = "내용은 비어있을 수 없습니다.")
    private String content;
    private Boolean anonymous;
    private Long venueId;
    private List<String> hashtag;

//    public static FreePost fromDTO(PostCreateRequestDTO dto, List<String> imageUrls, Member member, Venue venue) {
//        return FreePost.builder()
//                .title(dto.getTitle())
//                .content(dto.getContent())
//                .anonymous(dto.getAnonymous() != null && dto.getAnonymous())
//                .imageUrls(imageUrls)
//                .member(member)
//                .venue(venue)
//                .build();
//    }
}
