package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.entity.VenueReview;

import java.util.List;

public interface VenueReviewQueryRepository {
    // 필요한 쿼리 메서드를 정의합니다.

    /**
     * 특정 베뉴의 모든 리뷰를 정렬해서 조회합니다.
     * @param venueId 베뉴 ID
     * @param sortBy 정렬 기준 ("popular": 인기순, 그 외: 최신순)
     * @return 정렬된 베뉴 리뷰 목록
     */
    List<VenueReview> findAllReviewsSorted(Long venueId, String sortBy);
    
    /**
     * 특정 베뉴의 이미지가 있는 리뷰만 정렬해서 조회합니다.
     * @param venueId 베뉴 ID
     * @param sortBy 정렬 기준 ("popular": 인기순, 그 외: 최신순)
     * @return 정렬된 베뉴 리뷰 목록 (이미지 포함)
     */
    List<VenueReview> findReviewsWithImagesSorted(Long venueId, String sortBy);
    
    /**
     * 특정 베뉴의 모든 리뷰를 정렬해서 조회합니다 (차단된 멤버 제외).
     * @param venueId 베뉴 ID
     * @param sortBy 정렬 기준 ("popular": 인기순, 그 외: 최신순)
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 정렬된 베뉴 리뷰 목록 (차단된 멤버의 리뷰 제외)
     */
    List<VenueReview> findAllReviewsSortedExcludingBlocked(Long venueId, String sortBy, List<Long> blockedMemberIds);
    
    /**
     * 특정 베뉴의 이미지가 있는 리뷰만 정렬해서 조회합니다 (차단된 멤버 제외).
     * @param venueId 베뉴 ID
     * @param sortBy 정렬 기준 ("popular": 인기순, 그 외: 최신순)
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 정렬된 베뉴 리뷰 목록 (이미지 포함, 차단된 멤버의 리뷰 제외)
     */
    List<VenueReview> findReviewsWithImagesSortedExcludingBlocked(Long venueId, String sortBy, List<Long> blockedMemberIds);
}
