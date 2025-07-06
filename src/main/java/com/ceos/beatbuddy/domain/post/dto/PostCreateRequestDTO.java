package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCreateRequestDTO {
    private String title;
    @NotNull(message = "내용은 비어있을 수 없습니다.")
    @Size(min = 1, max = 1000, message = "내용은 1자 이상 1000자 이하로 작성해주세요.")
    private String content;

    private Boolean anonymous;
    private Long venueId;
    private List<String> hashtags;

    private int totalPrice;
    private int totalMembers;
    private LocalDateTime eventDate;


    public static FreePost toEntity(PostCreateRequestDTO dto, List<String> imageUrls, Member member, List<FixedHashtag> hashtag) {
        return FreePost.builder()
                .hashtag(hashtag)
                .imageUrls(imageUrls)
                .title(Optional.ofNullable(dto.getTitle()).orElse("")) // null 방지
                .content(dto.getContent())
                .anonymous(dto.getAnonymous())
                .member(member)
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