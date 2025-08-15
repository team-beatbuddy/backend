package com.ceos.beatbuddy.domain.venue.repository;

import com.ceos.beatbuddy.domain.coupon.domain.QCoupon;
import com.ceos.beatbuddy.domain.heartbeat.entity.QHeartbeat;
import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.vector.entity.Vector;
import com.ceos.beatbuddy.domain.venue.dto.VenueInfoOptimizedData;
import com.ceos.beatbuddy.domain.venue.entity.QVenue;
import com.ceos.beatbuddy.domain.venue.entity.QVenueGenre;
import com.ceos.beatbuddy.domain.venue.entity.QVenueMood;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Repository
@RequiredArgsConstructor
public class VenueInfoQueryRepositoryImpl implements VenueInfoQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public VenueInfoOptimizedData findVenueInfoOptimized(Long venueId, Long memberId) {
        QVenue venue = QVenue.venue;
        QHeartbeat heartbeat = QHeartbeat.heartbeat;
        QMember member = QMember.member;
        QVenueGenre venueGenre = QVenueGenre.venueGenre;
        QVenueMood venueMood = QVenueMood.venueMood;
        QCoupon coupon = QCoupon.coupon;

        var result = queryFactory
                .select(
                        venue,
                        JPAExpressions
                                .selectFrom(heartbeat)
                                .where(heartbeat.member.id.eq(memberId)
                                        .and(heartbeat.venue.eq(venue)))
                                .exists(),
                        venueGenre.genreVectorString,
                        venueMood.moodVectorString,
                        JPAExpressions
                                .selectFrom(coupon)
                                .where(coupon.venues.any().id.eq(venueId)
                                        .and(coupon.expireDate.after(LocalDate.now())))
                                .exists()
                )
                .from(venue)
                .leftJoin(venueGenre).on(venueGenre.venue.eq(venue))
                .leftJoin(venueMood).on(venueMood.venue.eq(venue))
                .where(venue.id.eq(venueId))
                .fetchFirst();

        if (result == null) {
            return null;
        }

        Venue venueEntity = result.get(venue);
        boolean isHeartbeat = result.get(1, Boolean.class);
        String genreVectorString = result.get(venueGenre.genreVectorString);
        String moodVectorString = result.get(venueMood.moodVectorString);
        boolean hasCoupon = result.get(4, Boolean.class);

        List<String> tagList = new ArrayList<>();
        if (genreVectorString != null) {
            List<Double> genreElements = parseVectorString(genreVectorString);
            Vector genreVector = new Vector(genreElements);
            tagList.addAll(Vector.getTrueGenreElements(genreVector));
        }
        if (moodVectorString != null) {
            List<Double> moodElements = parseVectorString(moodVectorString);
            Vector moodVector = new Vector(moodElements);
            tagList.addAll(Vector.getTrueMoodElements(moodVector));
        }
        tagList.add(venueEntity.getRegion().getText());

        return VenueInfoOptimizedData.builder()
                .venue(venueEntity)
                .isHeartbeat(isHeartbeat)
                .genreVector(genreVectorString)
                .moodVector(moodVectorString)
                .hasCoupon(hasCoupon)
                .tagList(tagList)
                .build();
    }

    private List<Double> parseVectorString(String vectorString) {
        return Stream.of(vectorString.replace("[", "").replace("]", "").split(","))
                .map(String::trim)
                .map(Double::parseDouble)
                .collect(Collectors.toList());
    }
}