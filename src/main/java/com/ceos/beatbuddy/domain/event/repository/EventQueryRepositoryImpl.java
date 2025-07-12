package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.QEvent;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.QEventLike;
import com.ceos.beatbuddy.domain.venue.entity.QVenue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findUpcomingEvents(String sort, int offset, int limit, List<String> regions) {
        QEvent event = QEvent.event;
        QEventLike eventLike = QEventLike.eventLike;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.startDate.gt(LocalDateTime.now()));

        if ("popular".equals(sort)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime yesterday = now.minusDays(1);

            return queryFactory
                    .select(event)
                    .from(event)
                    .leftJoin(event.venue, QVenue.venue).fetchJoin()
                    .leftJoin(eventLike).on(eventLike.event.eq(event).and(eventLike.createdAt.between(yesterday, now)))
                    .where(builder)
                    .groupBy(event)
                    .orderBy(eventLike.count().desc())
                    .offset(offset)
                    .limit(limit)
                    .fetch();
        }

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(builder)
                .orderBy(event.startDate.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public int countUpcomingEvents(List<String> regions) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.startDate.gt(LocalDateTime.now()));

        Long count = queryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }



    @Override
    public List<Event> findNowEvents(String sort, int offset, int limit, List<String> regions) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.startDate.loe(LocalDateTime.now()))
                .and(event.endDate.goe(LocalDateTime.now()));

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(builder)
                .orderBy(event.endDate.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }


    @Override
    public int countNowEvents(List<String> regions) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.startDate.loe(LocalDateTime.now()))
                .and(event.endDate.goe(LocalDateTime.now()));

        Long count = queryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    @Override
    public int countPastEvents(List<String> regions) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.endDate.lt(LocalDateTime.now()));

        Long count = queryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne();

        return count != null ? count.intValue() : 0;
    }

    @Override
    public List<Event> findPastEvents(String sort, int offset, int limit, List<String> regions) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = buildRegionFilter(event, regions)
                .and(event.endDate.lt(LocalDateTime.now()));

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(builder)
                .orderBy(event.endDate.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    private BooleanBuilder buildRegionFilter(QEvent event, List<String> regions) {
        BooleanBuilder regionBuilder = new BooleanBuilder();
        if (regions != null && !regions.isEmpty()) {
            for (String region : regions) {
                regionBuilder.or(event.region.eq(Event.Region.of(region)));
            }
        }
        return regionBuilder;
    }

    @Override
    public List<Event> findMyUpcomingEvents(Member member) {
        QEvent event = QEvent.event;
        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(event.host.eq(member)
                        .and(event.startDate.gt(LocalDateTime.now())))
                .orderBy(event.startDate.asc())
                .fetch();
    }

    @Override
    public List<Event> findMyNowEvents(Member member) {
        QEvent event = QEvent.event;
        LocalDateTime today = LocalDateTime.now();
        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(event.host.eq(member)
                        .and(event.startDate.loe(today))
                        .and(event.endDate.goe(today)))
                .orderBy(event.startDate.asc())
                .fetch();
    }

    @Override
    public List<Event> findMyPastEvents(Member member) {
        QEvent event = QEvent.event;
        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(event.host.eq(member)
                        .and(event.endDate.lt(LocalDateTime.now())))
                .orderBy(event.startDate.desc())
                .fetch();
    }

    @Override
    public List<Event> findEventsInPeriod(LocalDate startDate, LocalDate endDate, int offset, int limit) {
        QEvent event = QEvent.event;

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999


        return queryFactory
                .selectFrom(event)
                .where(
                        event.startDate.loe(endDateTime)
                                .and(event.endDate.goe(startDateTime))
                )
                .orderBy(event.startDate.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public long countEventsInPeriod(LocalDate startDate, LocalDate endDate) {
        QEvent event = QEvent.event;

        LocalDateTime startDateTime = startDate.atStartOfDay(); // 00:00
        LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // 23:59:59.999999999

        return queryFactory
                .selectFrom(event)
                .where(
                        event.startDate.loe(endDateTime)
                                .and(event.endDate.goe(startDateTime))
                )
                .fetchCount();
    }

    @Override
    public Page<Event> findVenueOngoingOrUpcomingEvents(Long venueId, Pageable pageable) {
        QEvent event = QEvent.event;
        LocalDateTime today = LocalDateTime.now();

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.venue.id.eq(venueId))
                .and(event.endDate.goe(today));

        List<Event> content = queryFactory
                .selectFrom(event)
                .where(builder)
                .orderBy(event.startDate.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = Optional.ofNullable(queryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne()).orElse(0L);

        return new PageImpl<>(content, pageable, count);
    }

    @Override
    public Page<Event> findVenuePastEvents(Long venueId, Pageable pageable) {
        QEvent event = QEvent.event;
        LocalDateTime today = LocalDateTime.now();

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.venue.id.eq(venueId))
                .and(event.endDate.lt(today));

        List<Event> content = queryFactory
                .selectFrom(event)
                .where(builder)
                .orderBy(event.endDate.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = Optional.ofNullable(queryFactory
                .select(event.count())
                .from(event)
                .where(builder)
                .fetchOne()) //결과 "한 줄"을 가져옴 → count(Long)
                .orElse(0L);

        return new PageImpl<>(content, pageable, count);
    }

    @Override
    public int countVenueOngoingOrUpcomingEvents(Long venueId) {
        QEvent event = QEvent.event;
        LocalDateTime today = LocalDateTime.now();

        return Objects.requireNonNull(queryFactory
                .select(event.count())
                .from(event)
                .where(event.venue.id.eq(venueId)
                        .and(event.endDate.goe(today)))
                .fetchOne()).intValue();
    }

    @Override
    public int countVenuePastEvents(Long venueId) {
        QEvent event = QEvent.event;
        LocalDateTime today = LocalDateTime.now();

        return Objects.requireNonNull(queryFactory
                .select(event.count())
                .from(event)
                .where(event.venue.id.eq(venueId)
                        .and(event.endDate.lt(today)))
                .fetchOne()).intValue();
    }

    @Override
    public List<Event> findEventsByVenueOrderByPopularity(Long venueId, Pageable pageable) {
        QEvent event = QEvent.event;
        QEventLike like = QEventLike.eventLike;
        LocalDateTime now = LocalDateTime.now();

        NumberExpression<Integer> statusOrder = new CaseBuilder()
                .when(event.startDate.loe(now).and(event.endDate.goe(now))).then(0)
                .when(event.startDate.gt(now)).then(1)
                .otherwise(2);

        return queryFactory
                .select(event)
                .from(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .leftJoin(like).on(like.event.eq(event))
                .where(event.venue.id.eq(venueId))
                .groupBy(event)
                .orderBy(
                        like.count().desc(),
                        statusOrder.asc()
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }
}
