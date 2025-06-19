package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.post.entity.QPost;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
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

}
