package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.VenueReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueReviewLikeRepository extends JpaRepository<VenueReviewLike, Long> {
    boolean existsByVenueReview_IdAndMember_Id(Long venueReviewId, Long memberId);
    int deleteByVenueReview_IdAndMember_Id(Long venueReviewId, Long memberId);
    
    // 벌크 조회용
    List<VenueReviewLike> findAllByMember_IdAndVenueReview_IdIn(Long memberId, List<Long> venueReviewIds);
}
