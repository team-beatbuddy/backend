package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;

import java.util.List;

public interface EventQueryRepository {
    List<Event> findUpcomingEvents(String sort, int offset, int limit);

    int countUpcomingEvents();
}
