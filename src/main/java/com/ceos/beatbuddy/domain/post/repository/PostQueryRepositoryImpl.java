package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.*;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PostQueryRepositoryImpl implements PostQueryRepository{
    private final JPAQueryFactory queryFactory;
    private final QPost post = QPost.post;

    @Override
    public List<Post> findHotPostsWithin12Hours() {
        LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(12);

        return queryFactory
                .selectFrom(post)
                .where(post.createdAt.goe(twelveHoursAgo))
                .orderBy(post.likes.add(post.comments).desc())
                .limit(2)
                .fetch();
    }

    @Override
    public Page<FreePost> findPostsByHashtags(List<FixedHashtag> hashtags, Pageable pageable) {
        QFreePost freePost = QFreePost.freePost;

        // 해시태그가 null이거나 비어있으면 빈 페이지 반환 (핸들러와 일관성 유지)
        if (hashtags == null || hashtags.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // where 조건을 변수로 추출하여 중복 제거
        BooleanExpression whereCondition = freePost.hashtag.any().in(hashtags);

        // 최신순 정렬
        JPQLQuery<FreePost> query = queryFactory
                .selectFrom(freePost)
                .where(whereCondition)
                .orderBy(freePost.createdAt.desc());

        List<FreePost> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(freePost.count())
                .from(freePost)
                .where(whereCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0L);
    }

    @Override
    public Page<FreePost> readAllPostsByUserExcludingAnonymous(Long userId, Pageable pageable) {
        QFreePost post = QFreePost.freePost;

        // 데이터 조회
        List<FreePost> content = queryFactory
                .selectFrom(post)
                .where(post.member.id.eq(userId).and(post.anonymous.isFalse()))
                .orderBy(post.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        Long count = queryFactory
                .select(post.count())
                .from(post)
                .where(post.member.id.eq(userId).and(post.anonymous.isFalse()))
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0);
    }

    @Override
    public List<Post> findHotPostsWithin12HoursExcludingBlocked(List<Long> blockedMemberIds) {
        LocalDateTime twelveHoursAgo = LocalDateTime.now().minusHours(12);

        return queryFactory
                .selectFrom(post)
                .where(
                        post.createdAt.goe(twelveHoursAgo),
                        blockedMemberIds.isEmpty() ? null : post.member.id.notIn(blockedMemberIds)
                )
                .orderBy(post.likes.add(post.comments).desc())
                .limit(2)
                .fetch();
    }

    @Override
    public Page<FreePost> findPostsByHashtagsExcludingBlocked(List<FixedHashtag> hashtags, Pageable pageable, List<Long> blockedMemberIds) {
        QFreePost freePost = QFreePost.freePost;

        // 해시태그가 null이거나 비어있으면 빈 페이지 반환 (핸들러와 일관성 유지)
        if (hashtags == null || hashtags.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        // where 조건을 변수로 추출하여 중복 제거
        BooleanExpression whereCondition = freePost.hashtag.any().in(hashtags);
        if (!blockedMemberIds.isEmpty()) {
            whereCondition = whereCondition.and(freePost.member.id.notIn(blockedMemberIds));
        }

        // 최신순 정렬
        JPQLQuery<FreePost> query = queryFactory
                .selectFrom(freePost)
                .where(whereCondition)
                .orderBy(freePost.createdAt.desc());

        List<FreePost> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = queryFactory
                .select(freePost.count())
                .from(freePost)
                .where(whereCondition)
                .fetchOne();

        return new PageImpl<>(content, pageable, count != null ? count : 0L);
    }
}