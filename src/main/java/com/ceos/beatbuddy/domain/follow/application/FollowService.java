package com.ceos.beatbuddy.domain.follow.application;

import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.follow.entity.FollowCreatedEvent;
import com.ceos.beatbuddy.domain.follow.exception.FollowErrorCode;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberBlockRepository;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;
    private final MemberBlockRepository memberBlockRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public FollowResponseDTO follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CustomException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(FollowErrorCode.FOLLOWING_TARGET_NOT_FOUND));

        if (followRepository.existsByFollower_IdAndFollowing_Id(followerId, followingId)) {
            throw new CustomException(FollowErrorCode.ALREADY_FOLLOWED);
        }

        Follow follow = Follow.builder()
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);

        // ======== 알림 전송
        eventPublisher.publishEvent(new FollowCreatedEvent(follower, following));

        return FollowResponseDTO.toDTO(follow);
    }


    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollower_IdAndFollowing_Id(followerId, followingId)
                .orElseThrow(() -> new CustomException(FollowErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    // 내가 팔로잉한 사람들 (차단된 사용자 제외)
    public List<FollowResponseDTO> getFollowings(Long memberId) {
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 차단된 사용자 ID 목록 조회
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(memberId);
        
        List<Follow> follows;
        if (blockedMemberIds.isEmpty()) {
            follows = followRepository.findAllByFollower(targetMember);
        } else {
            follows = followRepository.findAllByFollowerExcludingBlocked(targetMember, blockedMemberIds);
        }

        return follows.stream().map(FollowResponseDTO::fromFollowingMember).toList();
    }


    // 나를 팔로우한 사람들 (차단된 사용자 제외)
    public List<FollowResponseDTO> getFollowers(Long memberId) {
        Member targetMember = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 차단된 사용자 ID 목록 조회
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(memberId);
        
        List<Follow> follows;
        if (blockedMemberIds.isEmpty()) {
            follows = followRepository.findAllByFollowing(targetMember);
        } else {
            follows = followRepository.findAllByFollowingExcludingBlocked(targetMember, blockedMemberIds);
        }

        return follows.stream().map(FollowResponseDTO::fromFollowerMember).toList();
    }
}
