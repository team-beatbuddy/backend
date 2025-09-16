package com.ceos.beatbuddy.domain.follow.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.Getter;

public record FollowCreatedEvent(Member follower, Member following) {
    public Long getFollowerId() {
        return follower.getId();
    }

    public Long getFollowingId() {
        return following.getId();
    }

    public String getMessage() {
        return follower.getNickname() + "님이 회원님을 팔로우하기 시작했습니다.";
    }
}