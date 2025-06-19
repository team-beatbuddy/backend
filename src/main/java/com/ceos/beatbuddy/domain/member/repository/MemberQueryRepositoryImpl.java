package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.follow.entity.QFollow;
import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.post.entity.QPost;
import com.ceos.beatbuddy.global.CustomException;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import static com.ceos.beatbuddy.domain.follow.entity.QFollow.follow;
import static com.ceos.beatbuddy.domain.member.entity.QMember.member;
import static com.ceos.beatbuddy.domain.post.entity.QPost.post;
@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository{
    private final JPAQueryFactory queryFactory;
    @Override
    public MemberProfileSummaryDTO getMemberSummary(Long memberId) {
        QMember m = member;
        QPost p = post;
        QFollow f = follow;

        // 멤버 유효한지 확인 후 예외 처리 진행
        boolean exists = queryFactory
                .selectOne()
                .from(m)
                .where(m.id.eq(memberId))
                .fetchFirst() != null;

        if (!exists) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);
        }

        String nickname = queryFactory
                .select(m.nickname)
                .from(m)
                .where(m.id.eq(memberId))
                .fetchOne();

        String profileImageUrl = queryFactory
                .select(m.profileImage)
                .from(m)
                .where(m.id.eq(memberId))
                .fetchOne();

        String role = queryFactory
                .select(m.role)
                .from(m)
                .where(m.id.eq(memberId))
                .fetchOne();

        Long postCountRaw = queryFactory
                .select(p.count())
                .from(p)
                .where(p.member.id.eq(memberId))
                .fetchOne();

        Long followerCountRaw = queryFactory
                .select(f.count())
                .from(f)
                .where(f.following.id.eq(memberId))
                .fetchOne();

        Long followingCountRaw = queryFactory
                .select(f.count())
                .from(f)
                .where(f.follower.id.eq(memberId))
                .fetchOne();


        // null인 경우 0으로 입력되도록 처리
        int postCount = postCountRaw != null ? postCountRaw.intValue() : 0;
        int followerCount = followerCountRaw != null ? followerCountRaw.intValue() : 0;
        int followingCount = followingCountRaw != null ? followingCountRaw.intValue() : 0;

        return MemberProfileSummaryDTO.toDTO(nickname, profileImageUrl, role, postCount, followerCount, followingCount);
    }
}
