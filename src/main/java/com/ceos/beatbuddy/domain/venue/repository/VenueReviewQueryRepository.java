package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.entity.VenueReview;

import java.util.List;

public interface VenueReviewQueryRepository {
    // 필요한 쿼리 메서드를 정의합니다.
    List<VenueReview> findAllReviewsSorted(Long venueId, String sortBy);
    List<VenueReview> findReviewsWithImagesSorted(Long venueId, String sortBy);
}
