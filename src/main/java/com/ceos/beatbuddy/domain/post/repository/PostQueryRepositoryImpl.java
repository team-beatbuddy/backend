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
                .orderBy(post.likes.add(post.scraps.size()).desc())
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
}
