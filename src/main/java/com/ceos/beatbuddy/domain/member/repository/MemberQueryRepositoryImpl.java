package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.follow.entity.QFollow;
import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.dto.response.MemberProfileSummaryDTO;
import com.ceos.beatbuddy.domain.member.entity.QMember;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.post.entity.QPost;
import com.ceos.beatbuddy.global.CustomException;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.ceos.beatbuddy.domain.member.entity.QMember.member;
import static com.ceos.beatbuddy.domain.post.entity.QPost.post;
@Repository
@RequiredArgsConstructor
public class MemberQueryRepositoryImpl implements MemberQueryRepository{
    private final JPAQueryFactory queryFactory;
    @Override
    public MemberProfileSummaryDTO getMemberSummary(Long memberId, boolean isOwnProfile) {
        QMember qMember = member;
        QPost qPost = post;
        QFollow qFollowerFollow = new QFollow("followerFollow");
        QFollow qFollowingFollow = new QFollow("followingFollow");

        Tuple result = queryFactory
                .select(
                        qMember.id,
                        qMember.postProfileInfo.postProfileNickname,
                        qMember.postProfileInfo.postProfileImageUrl,
                        qMember.role,
                        qMember.businessInfo.businessName,
                        qPost.id.countDistinct(),
                        qFollowerFollow.id.countDistinct(),
                        qFollowingFollow.id.countDistinct()

                )
                .from(qMember)
                .leftJoin(qPost).on(
                    qPost.member.id.eq(qMember.id)
                    .and(isOwnProfile ? null : qPost.anonymous.eq(false)) // 타인 조회시 익명 글 제외
                )
                .leftJoin(qFollowerFollow).on(qFollowerFollow.following.id.eq(qMember.id))
                .leftJoin(qFollowingFollow).on(qFollowingFollow.follower.id.eq(qMember.id))
                .where(qMember.id.eq(memberId))
                .groupBy(qMember.id, qMember.postProfileInfo.postProfileNickname, qMember.postProfileInfo.postProfileImageUrl, qMember.role, qMember.businessInfo.businessName)
                .fetchOne();

        if (result == null) {
            throw new CustomException(MemberErrorCode.MEMBER_NOT_EXIST);
        } else {
            // null인 경우 0으로 입력되도록 처리
            long id = result.get(qMember.id);
            String nickname = result.get(qMember.nickname);
            String profileImageUrl = result.get(qMember.profileImage);
            String postProfileNickname = result.get(qMember.postProfileInfo.postProfileNickname) == null ? null : result.get(qMember.postProfileInfo.postProfileNickname);
            String postProfileImageUrl = result.get(qMember.postProfileInfo.postProfileImageUrl) == null? null : result.get(qMember.postProfileInfo.postProfileImageUrl);
            Role role = result.get(qMember.role);
            String businessName = result.get(qMember.businessInfo.businessName);

            int postCount = Optional.ofNullable(result.get(qPost.id.countDistinct())).map(Number::intValue).orElse(0);
            int followerCount = Optional.ofNullable(result.get(qFollowerFollow.id.countDistinct())).map(Number::intValue).orElse(0);
            int followingCount = Optional.ofNullable(result.get(qFollowingFollow.id.countDistinct())).map(Number::intValue).orElse(0);

            // 게시물 프로필 생성 여부 확인 (닉네임이나 이미지가 있으면 생성된 것으로 판단)
            boolean isPostProfileCreated = (postProfileNickname != null && !postProfileNickname.trim().isEmpty()) ||
                                          (postProfileImageUrl != null && !postProfileImageUrl.trim().isEmpty());

            // role이 BUSINESS인 경우 businessName을 사용, 아니면 null
            String displayBusinessName = (role == Role.BUSINESS) ? businessName : null;

            return MemberProfileSummaryDTO.toDTO(id, nickname, profileImageUrl, postProfileNickname, postProfileImageUrl, role.toString(),
                                               postCount, followerCount, followingCount, displayBusinessName, isPostProfileCreated);
        }
    }
}
