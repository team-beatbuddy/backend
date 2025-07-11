package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.follow.entity.QFollow;
import com.ceos.beatbuddy.domain.member.constant.Role;
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
import java.util.Optional;

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
                        qFollowerFollow.id.countDistinct(),
                        qFollowingFollow.id.countDistinct()

                )
                .from(qMember)
                .leftJoin(qPost).on(qPost.member.id.eq(qMember.id))
                .leftJoin(qFollowerFollow).on(qFollowerFollow.following.id.eq(qMember.id))
                .leftJoin(qFollowingFollow).on(qFollowingFollow.follower.id.eq(qMember.id))
                .where(qMember.id.eq(memberId))
                .groupBy(qMember.id, qMember.nickname, qMember.profileImage, qMember.role)
                .fetchOne();

        if (result == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);
        } else {
            // null인 경우 0으로 입력되도록 처리
            long id = result.get(qMember.id);
            String nickname = result.get(qMember.nickname);
            String profileImage = result.get(qMember.profileImage);
            Role role = result.get(qMember.role);

            int postCount = Optional.ofNullable(result.get(qPost.id.countDistinct())).map(Number::intValue).orElse(0);
            int followerCount = Optional.ofNullable(result.get(qFollowerFollow.id.countDistinct())).map(Number::intValue).orElse(0);
            int followingCount = Optional.ofNullable(result.get(qFollowingFollow.id.countDistinct())).map(Number::intValue).orElse(0);

            return MemberProfileSummaryDTO.toDTO(id, nickname, profileImage, role.toString(), postCount, followerCount, followingCount);
        }
    }
}
