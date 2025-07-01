package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLikeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueReviewLikeRepository extends JpaRepository<VenueReviewLike, VenueReviewLikeId> {

    boolean existsByVenueReview_IdAndMember_Id(Long venueReviewId, Long memberId);
}
