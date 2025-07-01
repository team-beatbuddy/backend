package com.ceos.beatbuddy.domain.scrapandlike.entity;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class VenueReviewLikeId implements Serializable {
    private Long memberId;
    private Long venueReviewId;

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public void setVenueReviewId(Long venueReviewId) {
        this.venueReviewId = venueReviewId;
    }
}
