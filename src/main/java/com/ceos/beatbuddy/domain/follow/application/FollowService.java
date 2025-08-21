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

        return FollowResponseDTO.fromFollowingMember(follow);
    }


    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFollower_IdAndFollowing_Id(followerId, followingId)
                .orElseThrow(() -> new CustomException(FollowErrorCode.FOLLOW_NOT_FOUND));

        followRepository.delete(follow);
    }

    // 팔로잉 목록 조회 (차단된 사용자 제외, isFollowing 정보 포함)
    public List<FollowResponseDTO> getFollowings(Long targetMemberId, Long currentMemberId) {
        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 차단된 사용자 ID 목록 조회 (조회 대상 사용자 기준)
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(targetMemberId);
        
        List<Follow> follows;
        if (blockedMemberIds.isEmpty()) {
            follows = followRepository.findAllByFollower(targetMember);
        } else {
            follows = followRepository.findAllByFollowerExcludingBlocked(targetMember, blockedMemberIds);
        }

        // 현재 사용자가 팔로우하고 있는 사용자들 ID 조회
        Set<Long> currentUserFollowingIds = followRepository.findFollowingMemberIds(currentMemberId);

        return follows.stream()
                .map(follow -> {
                    FollowResponseDTO dto = FollowResponseDTO.fromFollowingMember(follow);
                    // 현재 사용자가 이 사용자를 팔로우하고 있는지 확인
                    dto.setFollowing(currentUserFollowingIds.contains(dto.getMemberId()));
                    // 이 사용자는 현재 사용자를 팔로우 하고 있는지 여부
                    dto.setFollower(followRepository.existsByFollower_IdAndFollowing_Id(dto.getMemberId(), currentMemberId));
                    return dto;
                })
                .toList();
    }


    // 팔로워 목록 조회 (차단된 사용자 제외, isFollowing 정보 포함)
    public List<FollowResponseDTO> getFollowers(Long targetMemberId, Long currentMemberId) {
        Member targetMember = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 차단된 사용자 ID 목록 조회 (조회 대상 사용자 기준)
        Set<Long> blockedMemberIds = memberBlockRepository.findBlockedMemberIdsByBlockerId(targetMemberId);
        
        List<Follow> follows;
        if (blockedMemberIds.isEmpty()) {
            follows = followRepository.findAllByFollowing(targetMember);
        } else {
            follows = followRepository.findAllByFollowingExcludingBlocked(targetMember, blockedMemberIds);
        }

        // 현재 사용자가 팔로우하고 있는 사용자들 ID 조회
        Set<Long> currentUserFollowingIds = followRepository.findFollowingMemberIds(currentMemberId);

        return follows.stream()
                .map(follow -> {
                    FollowResponseDTO dto = FollowResponseDTO.fromFollowerMember(follow);
                    // 현재 사용자가 이 사용자를 팔로우하고 있는지 확인
                    dto.setFollowing(currentUserFollowingIds.contains(dto.getMemberId()));
                    // 이 사용자는 현재 사용자를 팔로우 하고 있는지 여부
                    dto.setFollower(followRepository.existsByFollower_IdAndFollowing_Id(currentMemberId, dto.getMemberId()));
                    return dto;
                })
                .toList();
    }
}
