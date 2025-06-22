package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDTO {
    @NotNull(message = "제목은 비어있을 수 없습니다.")
    private String title;
    @NotNull(message = "내용은 비어있을 수 없습니다.")
    private String content;

    private Boolean anonymous;
    private Long venueId;
    private List<String> hashtag;

    private int totalPrice;
    private int totalMembers;
    private LocalDateTime eventDate;


    public static FreePost toEntity(PostCreateRequestDTO dto, List<String> imageUrls, Member member, Venue venue) {
        return FreePost.builder()
                .hashtag(dto.getHashtag())
                .imageUrls(imageUrls)
                .title(dto.getTitle())
                .content(dto.getContent())
                .anonymous(dto.getAnonymous())
                .member(member)
                .venue(venue)
                .build();
    }

    public static PiecePost toEntity(PostCreateRequestDTO dto, List<String> imageUrls, Member member, Venue venue, Piece piece) {
        return PiecePost.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .imageUrls(imageUrls)
                .member(member)
                .anonymous(dto.getAnonymous())
                .venue(venue)
                .piece(piece)
                .build();
    }
}