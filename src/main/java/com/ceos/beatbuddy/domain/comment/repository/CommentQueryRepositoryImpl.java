package com.ceos.beatbuddy.domain.comment.repository;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import com.ceos.beatbuddy.domain.comment.entity.QComment;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CommentQueryRepositoryImpl implements CommentQueryRepository {
    
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Comment> findAllByPostIdExcludingBlocked(Long postId, Pageable pageable, List<Long> blockedMemberIds) {
        QComment comment = QComment.comment;

        BooleanExpression whereCondition = comment.post.id.eq(postId);
        if (!blockedMemberIds.isEmpty()) {
            whereCondition = whereCondition.and(comment.member.id.notIn(blockedMemberIds));
        }

        List<Comment> content = queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.createdAt.asc()) // 댓글은 일반적으로 오래된 순으로 정렬
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(comment.count())
                .from(comment)
                .where(whereCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0L);
    }

    @Override
    public List<Comment> findAllByMemberIdAndPostIdInExcludingBlocked(Long memberId, List<Long> postIds, List<Long> blockedMemberIds) {
        QComment comment = QComment.comment;

        BooleanExpression whereCondition = comment.member.id.eq(memberId)
                .and(comment.post.id.in(postIds));
        
        if (!blockedMemberIds.isEmpty()) {
            whereCondition = whereCondition.and(comment.member.id.notIn(blockedMemberIds));
        }

        return queryFactory
                .selectFrom(comment)
                .where(whereCondition)
                .orderBy(comment.createdAt.desc())
                .fetch();
    }
}