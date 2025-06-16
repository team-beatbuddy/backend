package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.EventScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventScrapRepository extends JpaRepository<EventScrap, EventInteractionId> {
    Integer countAllByEvent(Event event);

    boolean existsById(EventInteractionId id);

    List<EventScrap> findAllByMemberId(Long memberId);
}
