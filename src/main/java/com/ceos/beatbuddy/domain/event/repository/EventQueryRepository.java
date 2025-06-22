package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;

import java.util.List;
import java.util.Map;

public interface EventQueryRepository {
    // 예정 이벤트
    List<Event> findUpcomingEvents(String sort, int offset, int limit);
    int countUpcomingEvents();

    // 진행 중 이벤트
    List<Event> findNowEvents(String sort, int offset, int limit);
    int countNowEvents();


    // 완룓된 이벤트
    List<Event> findPastEvents(String sort, int offset, int limit);
    int countPastEvents();

    Map<String, List<Event>> findPastEventsGroupedByMonth();


}
