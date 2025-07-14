package com.ceos.beatbuddy.domain.follow.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;

public record FollowCreatedEvent(Member follower, Member following) {}