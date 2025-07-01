package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.entity.QVenueReview;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@RequiredArgsConstructor
public class VenueReviewQueryRepositoryImpl implements VenueReviewQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<VenueReview> findAllReviewsSorted(Long venueId, String sortBy) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .where(review.venue.id.eq(venueId))
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    @Override
    public List<VenueReview> findReviewsWithImagesSorted(Long venueId, String sortBy) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .where(
                        review.venue.id.eq(venueId),
                        review.imageUrls.isNotEmpty()
                )
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    private OrderSpecifier<?> getOrderSpecifier(QVenueReview review, String sortBy) {
        if ("popular".equals(sortBy)) {
            return review.likes.desc();
        } else {
            return review.createdAt.desc(); // 기본은 최신순
        }
    }
}
