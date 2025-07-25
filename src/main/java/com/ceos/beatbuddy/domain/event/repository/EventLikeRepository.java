package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface EventLikeRepository extends JpaRepository<EventLike, EventInteractionId> {
    Integer countAllByEvent(Event event);

    List<EventLike> findByMember(Member member);

    @Query("SELECT el.event.id FROM EventLike el WHERE el.member = :member")
    Set<Long> findLikedEventIdsByMember(@Param("member") Member member);
}
