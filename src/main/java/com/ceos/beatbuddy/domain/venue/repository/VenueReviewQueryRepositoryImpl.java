package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.entity.QVenueReview;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class VenueReviewQueryRepositoryImpl implements VenueReviewQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<VenueReview> findReviewsWithImages(Long venueId) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .where(
                        review.venue.id.eq(venueId),
                        review.imageUrls.isNotEmpty() // 조건부 필터링
                )
                .fetch();
    }
}
