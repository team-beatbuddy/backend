package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.follow.entity.QFollow;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.response.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.post.entity.QPost;
import com.ceos.beatbuddy.global.CustomException;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
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
public class MemberQueryRepositoryImpl implements MemberQueryRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public MemberProfileSummaryDTO getMemberSummary(Long memberId, boolean isOwnProfile) {
        QMember qMember = member;
        QPost qPost = post;
        QFollow qFollow = follow;

        // 1) postCount 서브쿼리 (본인=전체, 타인=익명 아님만)
        BooleanExpression visibleForOthers = qPost.anonymous.isFalse().or(qPost.anonymous.isNull());
        JPQLQuery<Long> postCountSub = JPAExpressions
                .select(qPost.id.countDistinct())
                .from(qPost)
                .where(qPost.member.id.eq(qMember.id)
                        .and(isOwnProfile ? Expressions.TRUE.isTrue() : visibleForOthers));

        // 2) follower/following 카운트도 서브쿼리로
        JPQLQuery<Long> followerCountSub = JPAExpressions
                .select(qFollow.id.countDistinct())
                .from(qFollow)
                .where(qFollow.following.id.eq(qMember.id));

        JPQLQuery<Long> followingCountSub = JPAExpressions
                .select(qFollow.id.countDistinct())
                .from(qFollow)
                .where(qFollow.follower.id.eq(qMember.id));

        // 3) 필요한 프로필 컬럼을 명시적으로 select
        Tuple result = queryFactory
                .select(
                        qMember.id,
                        qMember.nickname,
                        qMember.profileImage,
                        qMember.postProfileInfo.postProfileNickname,
                        qMember.postProfileInfo.postProfileImageUrl,
                        qMember.role,
                        qMember.businessInfo.businessName,
                        postCountSub,
                        followerCountSub,
                        followingCountSub
                )
                .from(qMember)
                .where(qMember.id.eq(memberId))
                .fetchOne();

        if (result == null) throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);

        long id = result.get(qMember.id);
        String nickname = result.get(qMember.nickname);
        String profileImageUrl = result.get(qMember.profileImage);
        String postProfileNickname = result.get(qMember.postProfileInfo.postProfileNickname);
        String postProfileImageUrl = result.get(qMember.postProfileInfo.postProfileImageUrl);
        Role role = result.get(qMember.role);
        String businessName = result.get(qMember.businessInfo.businessName);

        int postCount = Optional.ofNullable(result.get(postCountSub)).map(Long::intValue).orElse(0);
        int followerCount = Optional.ofNullable(result.get(followerCountSub)).map(Long::intValue).orElse(0);
        int followingCount = Optional.ofNullable(result.get(followingCountSub)).map(Long::intValue).orElse(0);

        boolean isPostProfileCreated =
                (postProfileNickname != null && !postProfileNickname.isBlank()) ||
                        (postProfileImageUrl != null && !postProfileImageUrl.isBlank());

        String displayBusinessName = (role == Role.BUSINESS) ? businessName : null;

        return MemberProfileSummaryDTO.toDTO(
                id, nickname, profileImageUrl,
                postProfileNickname, postProfileImageUrl,
                Objects.requireNonNull(role).toString(),
                postCount, followerCount, followingCount,
                displayBusinessName, isPostProfileCreated
        );
    }
}