package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.follow.entity.QFollow;
import com.ceos.beatbuddy.domain.member.dto.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.post.entity.QPost;
import com.ceos.beatbuddy.global.CustomException;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Objects;

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

        Tuple result = queryFactory
                .select(
                        m.id,
                        m.nickname,
                        m.role,
                        p.id.countDistinct(),
                        f.follower.id.countDistinct(),
                        f.following.id.countDistinct()

                )
                .from(m)
                .leftJoin(p).on(p.member.id.eq(m.id))
                .leftJoin(f).on(f.follower.id.eq(m.id))
                .leftJoin(f).on(f.following.id.eq(m.id))
                .where(m.id.eq(memberId))
                .groupBy(m.id, m.nickname, m.profileImage, m.role)
                .fetchOne();

        if (result == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);
        } else {
            // null인 경우 0으로 입력되도록 처리
            int postCount = result.get(p.id.countDistinct()) != null ? result.get(p.id.countDistinct()).intValue() : 0;
            int followerCount = result.get(f.id.countDistinct()) != null ? result.get(f.id.countDistinct()).intValue() : 0;
            int followingCount = result.get(f.id.countDistinct()) != null ? result.get(f.id.countDistinct()).intValue() : 0;

            return MemberProfileSummaryDTO.toDTO(result.get(m.id).longValue(), result.get(m.nickname), result.get(m.profileImage), result.get(m.role), postCount, followerCount, followingCount);

        }
    }
}
