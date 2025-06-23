package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.QEvent;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.QEventLike;
import com.ceos.beatbuddy.domain.venue.entity.QVenue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findUpcomingEvents(String sort, int offset, int limit) {
        QEvent event = QEvent.event;
        QEventLike eventLike = QEventLike.eventLike;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.startDate.gt(LocalDate.now()));

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
    public List<Event> findNowEvents(String sort, int offset, int limit) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.startDate.loe(LocalDate.now()))
                .and(event.endDate.goe(LocalDate.now()));

        OrderSpecifier<?> orderBy = "popular".equals(sort)
                ? event.likes.desc()
                : event.startDate.asc();

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(builder)
                .orderBy(orderBy)
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
    public List<Event> findPastEvents(String sort, int offset, int limit) {
        QEvent event = QEvent.event;

        LocalDate now = LocalDate.now();
        BooleanBuilder builder = new BooleanBuilder()
                .and(event.endDate.lt(now));

        OrderSpecifier<?> orderBy = "popular".equals(sort)
                ? event.likes.desc()
                : event.endDate.desc();

        return queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(builder)
                .orderBy(orderBy)
                .offset(offset)
                .limit(limit)
                .fetch();
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
    public Map<String, List<Event>> findPastEventsGroupedByMonth() {
        QEvent event = QEvent.event;

        LocalDate now = LocalDate.now(); // 오늘
        LocalDate from = now.minusYears(1).withDayOfMonth(1); // 작년 같은 월의 1일
        LocalDate to = now.minusDays(1); // 어제까지

        List<Event> allEvents = queryFactory
                .selectFrom(event)
                .leftJoin(event.venue, QVenue.venue).fetchJoin()
                .where(event.endDate.between(from, to)) // 작년 ~ 어제까지
                .orderBy(event.likes.desc())
                .fetch();

        Map<String, List<Event>> result = new LinkedHashMap<>();
        for (Event e : allEvents) {
            String month = YearMonth.from(e.getEndDate()).toString(); // yyyy-MM
            result.computeIfAbsent(month, k -> new ArrayList<>()).add(e);
        }

        return result;
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
}
