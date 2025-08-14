package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.venue.entity.QVenueReview;
import com.ceos.beatbuddy.domain.venue.entity.VenueReview;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class VenueReviewQueryRepositoryImpl implements VenueReviewQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<VenueReview> findAllReviewsSorted(Long venueId, String sortBy) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.member).fetchJoin()
                .leftJoin(review.venue).fetchJoin()
                .where(review.venue.id.eq(venueId))
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    @Override
    public List<VenueReview> findReviewsWithImagesSorted(Long venueId, String sortBy) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.member).fetchJoin()
                .leftJoin(review.venue).fetchJoin()
                .where(
                        review.venue.id.eq(venueId),
                        review.imageUrls.isNotNull().and(review.imageUrls.ne(""))
                )
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    @Override
    public List<VenueReview> findAllReviewsSortedExcludingBlocked(Long venueId, String sortBy, Set<Long> blockedMemberIds) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.member).fetchJoin()
                .leftJoin(review.venue).fetchJoin()
                .where(
                        review.venue.id.eq(venueId),
                        blockedMemberIds.isEmpty() ? null : review.member.id.notIn(blockedMemberIds)
                )
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    @Override
    public List<VenueReview> findReviewsWithImagesSortedExcludingBlocked(Long venueId, String sortBy, Set<Long> blockedMemberIds) {
        QVenueReview review = QVenueReview.venueReview;

        return queryFactory
                .selectFrom(review)
                .leftJoin(review.member).fetchJoin()
                .leftJoin(review.venue).fetchJoin()
                .where(
                        review.venue.id.eq(venueId),
                        review.imageUrls.isNotNull().and(review.imageUrls.ne("")),
                        blockedMemberIds.isEmpty() ? null : review.member.id.notIn(blockedMemberIds)
                )
                .orderBy(getOrderSpecifier(review, sortBy))
                .fetch();
    }

    private static final String SORT_BY_POPULAR = "popular";

    private OrderSpecifier<?> getOrderSpecifier(QVenueReview review, String sortBy) {
        if (SORT_BY_POPULAR.equalsIgnoreCase(sortBy)) {
            return review.likes.desc();
        } else {
            return review.createdAt.desc(); // 기본은 최신순
        }
    }
}
