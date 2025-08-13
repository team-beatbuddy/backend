package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

@Repository
public interface VenueReviewLikeRepository extends JpaRepository<VenueReviewLike, Long> {
    boolean existsByVenueReview_IdAndMember_Id(Long venueReviewId, Long memberId);
    int deleteByVenueReview_IdAndMember_Id(Long venueReviewId, Long memberId);
}
