package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreePostRequestDTO implements PostCreateRequestDTO{
    private String title;
    private String content;
    private Boolean anonymous;
    private Long venueId;
    private List<String> hashtag;

    public static FreePost toEntity(FreePostRequestDTO dto, List<String> imageUrls, Member member, Venue venue) {
        return FreePost.builder()
                .hashtag(dto.hashtag())
                .imageUrls(imageUrls)
                .title(dto.title())
                .content(dto.content())
                .anonymous(dto.anonymous())
                .member(member)
                .venue(venue)
                .build();
    }
    @Override public String title() { return title; }
    @Override public String content() { return content; }
    @Override public Boolean anonymous() { return anonymous; }
    @Override public Long venueId() { return venueId; }

    public List<String> hashtag() {
        return hashtag;
    }
}
