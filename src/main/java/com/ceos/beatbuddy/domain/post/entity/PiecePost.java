package com.ceos.beatbuddy.domain.post.entity;


import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.util.List;

import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PiecePost extends Post{
    @ManyToOne
    @JoinColumn(name = "pieceId")
    private Piece piece;

    @ManyToOne
    @JoinColumn(name = "venueId")
    private Venue venue;
}
