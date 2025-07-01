package com.ceos.beatbuddy.domain.magazine.repository;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.magazine.entity.QMagazine;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class MagazineQueryRepositoryImpl implements MagazineQueryRepository {
    private final JPAQueryFactory queryFactory;
    private final QMagazine magazine = QMagazine.magazine;
    // isPinned 되고 isVisible 된 게시물들을 최신순으로 조회
    @Override
    public List<Magazine> findPinnedMagazines() {
        return queryFactory
                .selectFrom(magazine)
                .where(
                        magazine.isPinned.isTrue(),
                        magazine.isVisible.isTrue()
                )
                .orderBy(magazine.createdAt.desc())
                .limit(5)
                .fetch();
    }
}
