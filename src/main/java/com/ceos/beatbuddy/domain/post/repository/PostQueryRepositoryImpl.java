package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.*;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

        JPQLQuery<FreePost> query = queryFactory
                .selectFrom(freePost)
                .where(
                        hashtags == null || hashtags.isEmpty()
                                ? null
                                : freePost.hashtag.any().in(hashtags)
                )
                .orderBy(freePost.createdAt.desc());

        List<FreePost> content = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long count = queryFactory
                .select(freePost.count())
                .from(freePost)
                .where(
                        hashtags == null || hashtags.isEmpty()
                                ? null
                                : freePost.hashtag.any().in(hashtags)
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, count);
    }
}
