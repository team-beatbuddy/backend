package com.ceos.beatbuddy.domain.post.dto;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.post.entity.Piece;
import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
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
public class PiecePostRequestDTO implements PostCreateRequestDTO {

    private String title;
    private String content;
    private Boolean anonymous;
    private Long venueId;
    private int totalPrice;
    private int totalMembers;
    private LocalDateTime eventDate;

    public static PiecePost toEntity(PiecePostRequestDTO dto, List<String> imageUrls, Member member, Venue venue, Piece piece) {
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

    @Override public String title() { return title; }
    @Override public String content() { return content; }
    @Override public Boolean anonymous() { return anonymous; }
    @Override public Long venueId() { return venueId; }

    public int totalPrice() { return totalPrice; }
    public int totalMembers() { return totalMembers; }
    public LocalDateTime eventDate() { return eventDate; }
}
