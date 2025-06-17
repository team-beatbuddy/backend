package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.QEvent;
import com.ceos.beatbuddy.domain.venue.entity.QVenue;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class EventQueryRepositoryImpl implements EventQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Event> findUpcomingEvents(String sort, int offset, int limit) {
        QEvent event = QEvent.event;

        // 오늘 이후의 이벤트만 조회
        BooleanBuilder builder = new BooleanBuilder()
                .and(event.startDate.goe(LocalDate.now()));

        // 정렬 조건 분기
        OrderSpecifier<?> orderBy;

        switch (sort) {
            case "popular" -> orderBy = event.likes.desc();           // 좋아요 순
            case "latest" -> orderBy = event.startDate.desc();         // 날짜 빠른 순
            default -> orderBy = event.startDate.desc();               // 디폴트도 날짜 빠른 순
        }

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
    public int countUpcomingEvents() {
        QEvent event = QEvent.event;

        return Objects.requireNonNull(queryFactory
                        .select(event.count())
                        .from(event)
                        .where(event.startDate.goe(LocalDate.now()))
                        .fetchOne())
                .intValue();
    }


    @Override
    public List<Event> findPastEvents(String sort, int offset, int limit) {
        QEvent event = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder()
                .and(event.endDate.lt(LocalDate.now()));

        OrderSpecifier<?> orderBy = switch (sort) {
            case "popular" -> event.likes.desc();
            case "latest" -> event.endDate.desc();
            default -> event.endDate.desc();
        };

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
}