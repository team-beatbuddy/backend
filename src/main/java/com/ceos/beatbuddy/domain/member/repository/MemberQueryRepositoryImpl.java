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
        QMember qMember = member;
        QPost qPost = post;
        QFollow qFollowerFollow = new QFollow("followerFollow");
        QFollow qFollowingFollow = new QFollow("followingFollow");

        Tuple result = queryFactory
                .select(
                        qMember.id,
                        qMember.nickname,
                        qMember.profileImage,
                        qMember.role,
                        qPost.id.countDistinct(),
                        qFollowerFollow.follower.id.countDistinct(),
                        qFollowingFollow.following.id.countDistinct()

                )
                .from(qMember)
                .leftJoin(qPost).on(qPost.member.id.eq(qMember.id))
                .leftJoin(qFollowerFollow).on(qFollowerFollow.follower.id.eq(qMember.id))
                .leftJoin(qFollowingFollow).on(qFollowingFollow.following.id.eq(qMember.id))
                .where(qMember.id.eq(memberId))
                .groupBy(qMember.id, qMember.nickname, qMember.profileImage, qMember.role)
                .fetchOne();

        if (result == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);
        } else {
            // null인 경우 0으로 입력되도록 처리
            int postCount = result.get(qPost.id.countDistinct()) != null ? result.get(qPost.id.countDistinct()).intValue() : 0;
            int followerCount = result.get(qFollowerFollow.id.countDistinct()) != null ? result.get(qFollowerFollow.id.countDistinct()).intValue() : 0;
            int followingCount = result.get(qFollowingFollow.id.countDistinct()) != null ? result.get(qFollowingFollow.id.countDistinct()).intValue() : 0;

            return MemberProfileSummaryDTO.toDTO(result.get(qMember.id).longValue(), result.get(qMember.nickname), result.get(qMember.profileImage), result.get(qMember.role), postCount, followerCount, followingCount);

        }
    }
}
