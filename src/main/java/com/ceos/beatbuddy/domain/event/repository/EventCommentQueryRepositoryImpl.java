package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.QEventComment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventCommentQueryRepositoryImpl implements EventCommentQueryRepository {
    
    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventComment> findAllByEventExcludingBlocked(Event event, List<Long> blockedMemberIds) {
        QEventComment eventComment = QEventComment.eventComment;

        BooleanExpression whereCondition = eventComment.event.eq(event);
        if (!blockedMemberIds.isEmpty()) {
            whereCondition = whereCondition.and(eventComment.author.id.notIn(blockedMemberIds));
        }

        return queryFactory
                .selectFrom(eventComment)
                .where(whereCondition)
                .orderBy(eventComment.createdAt.asc()) // 댓글은 일반적으로 오래된 순으로 정렬
                .fetch();
    }
}