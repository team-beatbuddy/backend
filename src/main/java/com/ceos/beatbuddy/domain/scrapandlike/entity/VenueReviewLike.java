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
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"memberId", "venueReviewId"})
})
public class VenueReviewLike extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "memberId", nullable = true)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venueReviewId")
    private VenueReview venueReview;

    public static VenueReviewLike toEntity(Member member, VenueReview venueReview) {
        return VenueReviewLike.builder()
                .venueReview(venueReview)
                .member(member)
                .build();
    }

    public Long getVenueReviewId() {
        return this.venueReview.getId();
    }

    public Long getMemberId() {
        return this.member.getId();
    }
}
