package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.QEvent;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.QEventLike;
import com.ceos.beatbuddy.domain.venue.entity.QVenue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findUpcomingEvents(String sort, int offset, int limit, String region) {
        QEvent event = QEvent.event;
        QEventLike eventLike = QEventLike.eventLike;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.startDate.gt(LocalDate.now()));

        if (region != null) {
            builder.and(event.region.eq(Event.of(region)));
        }

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
    public int countUpcomingEvents() {
        QEvent event = QEvent.event;

        return Objects.requireNonNull(queryFactory
                        .select(event.count())
                        .from(event)
                        .where(event.startDate.gt(LocalDate.now()))
                        .fetchOne())
                .intValue();
    }


    @Override
    public List<Event> findNowEvents(String sort, int offset, int limit, String region) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.startDate.loe(LocalDate.now()))
                .and(event.endDate.goe(LocalDate.now()));

        if (region != null) {
            builder.and(event.region.eq(Event.of(region)));
        }

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
    public int countNowEvents() {
        QEvent event = QEvent.event;

        return Objects.requireNonNull(queryFactory
                        .select(event.count())
                        .from(event)
                        .where(event.startDate.loe(LocalDate.now())
                                .and(event.endDate.goe(LocalDate.now())))
                        .fetchOne())
                .intValue();
    }

    @Override
    public int countPastEvents() {
        QEvent event = QEvent.event;

        return Objects.requireNonNull(queryFactory
                .select(event.count())
                .from(event)
                .where(event.endDate.lt(LocalDate.now()))
                .fetchOne()).intValue();
    }

    @Override
    public List<Event> findPastEvents(String sort, int offset, int limit, String region) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.endDate.lt(LocalDate.now()));

        if (region != null) {
            builder.and(event.region.eq(Event.of(region)));
        }

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
    public List<Event> findMyUpcomingEvents(Member member) {
        QEvent event = QEvent.event;
        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(event.host.eq(member)
                        .and(event.startDate.gt(LocalDate.now())))
                .orderBy(event.startDate.asc())
                .fetch();
    }

    @Override
    public List<Event> findMyNowEvents(Member member) {
        QEvent event = QEvent.event;
        LocalDate today = LocalDate.now();
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
                        .and(event.endDate.lt(LocalDate.now())))
                .orderBy(event.startDate.desc())
                .fetch();
    }

    @Override
    public List<Event> findEventsInPeriod(LocalDate startDate, LocalDate endDate, int offset, int limit) {
        QEvent event = QEvent.event;

        return queryFactory
                .selectFrom(event)
                .where(
                        event.startDate.loe(endDate)
                                .and(event.endDate.goe(startDate))
                )
                .orderBy(event.startDate.asc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }

    @Override
    public long countEventsInPeriod(LocalDate startDate, LocalDate endDate) {
        QEvent event = QEvent.event;

        return queryFactory
                .selectFrom(event)
                .where(
                        event.startDate.loe(endDate)
                                .and(event.endDate.goe(startDate))
                )
                .fetchCount();
    }
}
