package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;

import java.time.LocalDate;
import java.util.List;

public interface EventQueryRepository {
    // 예정 이벤트
    List<Event> findUpcomingEvents(String sort, int offset, int limit, String region);
    int countUpcomingEvents();

    // 진행 중 이벤트
    List<Event> findNowEvents(String sort, int offset, int limit, String region);
    int countNowEvents();

    // 완룓된 이벤트
    List<Event> findPastEvents(String sort, int offset, int limit, String region);
    int countPastEvents();

    List<Event> findMyUpcomingEvents(Member member);
    List<Event> findMyNowEvents(Member member);
    List<Event> findMyPastEvents(Member member);
    List<Event> findEventsInPeriod(LocalDate startDate, LocalDate endDate, int offset, int limit);
    long countEventsInPeriod(LocalDate startDate, LocalDate endDate);


}
