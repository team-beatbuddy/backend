package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;

public record EventCommentCreatedEvent(
        Event event,
        Member member,
        EventComment comment,
        EventComment parent,
        boolean isStaff
) {}