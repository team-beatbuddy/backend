package com.ceos.beatbuddy.domain.event.entity;

import com.ceos.beatbuddy.domain.member.entity.Member;
import lombok.Getter;

@Getter
public record EventCommentCreatedEvent(
        Event event,
        Member member,
        EventComment comment,
        EventComment parent,
        boolean isStaff
) {}