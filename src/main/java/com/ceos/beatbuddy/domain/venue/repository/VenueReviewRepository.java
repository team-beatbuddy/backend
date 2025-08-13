package com.ceos.beatbuddy.domain.venue.repository;


import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VenueReviewRepository extends JpaRepository<VenueReview, Long> {
    List<VenueReview> findByVenueId(Long venueId);

    @Modifying
    @Query("UPDATE VenueReview v SET v.likes = v.likes + 1 WHERE v.id = :id")
    void increaseLikeCount(@Param("id") Long id);
    @Modifying
    @Query("UPDATE VenueReview v SET v.likes = CASE WHEN v.likes > 0 THEN v.likes - 1 ELSE 0 END WHERE v.id = :id")
    void decreaseLikeCount(@Param("id") Long id);
    
    // 좋아요 수를 지정된 개수만큼 감소
    @Modifying
    @Query("UPDATE VenueReview v SET v.likes = CASE WHEN v.likes >= :count THEN v.likes - :count ELSE 0 END WHERE v.id = :id")
    void decreaseLikeCount(@Param("id") Long id, @Param("count") int count);
}
