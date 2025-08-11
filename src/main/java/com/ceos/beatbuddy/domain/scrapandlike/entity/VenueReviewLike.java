package com.ceos.beatbuddy.domain.scrapandlike.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.ceos.beatbuddy.global.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VenueReviewLike extends BaseTimeEntity {
    @EmbeddedId
    private VenueReviewLikeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @MapsId("memberId")
    @JoinColumn(name = "memberId", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("venueReviewId")
    @JoinColumn(name = "venueReviewId")
    private VenueReview venueReview;

    public static VenueReviewLike toEntity(Member member, VenueReview venueReview) {
        return VenueReviewLike.builder()
                .id(new VenueReviewLikeId(member.getId(), venueReview.getId()))
                .venueReview(venueReview)
                .member(member)
                .build();
    }

    public Long getVenueReviewId() {
        return this.id.getVenueReviewId();
    }

    public Long getMemberId() {
        return this.id.getMemberId();
    }
}
