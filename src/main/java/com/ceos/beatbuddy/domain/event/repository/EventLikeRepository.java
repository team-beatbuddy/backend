package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventLikeRepository extends JpaRepository<EventLike, EventInteractionId> {
    Integer countAllByEvent(Event event);

    List<EventLike> findByMember(Member member);
}
