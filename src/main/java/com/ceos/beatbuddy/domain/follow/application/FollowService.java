package com.ceos.beatbuddy.domain.follow.application;

import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.follow.entity.FollowId;
import com.ceos.beatbuddy.domain.follow.exception.FollowErrorCode;
import com.ceos.beatbuddy.domain.follow.repository.FollowRepository;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.exception.MemberErrorCode;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public FollowResponseDTO follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new CustomException(FollowErrorCode.CANNOT_FOLLOW_SELF);
        }

        Member follower = memberRepository.findById(followerId)
                .orElseThrow(() -> new CustomException(MemberErrorCode.MEMBER_NOT_EXIST));

        // 팔로잉 할 상대가 존재하지 않는다
        Member following = memberRepository.findById(followingId)
                .orElseThrow(() -> new CustomException(FollowErrorCode.FOLLOWING_TARGET_NOT_FOUND));


        FollowId followId = new FollowId(followerId, followingId);

        if (followRepository.existsById(followId)) {
            throw new CustomException(FollowErrorCode.ALREADY_FOLLOWED);
        }

        Follow follow = Follow.builder()
                .id(followId)
                .follower(follower)
                .following(following)
                .build();

        followRepository.save(follow);

        return FollowResponseDTO.toDTO(follow);
    }

}
