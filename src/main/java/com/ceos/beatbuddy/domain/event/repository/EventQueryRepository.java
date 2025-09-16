package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventQueryRepository {
    // 예정 이벤트
    List<Event> findUpcomingEvents(String sort, int offset, int limit, List<String> region);
    int countUpcomingEvents(List<String> regions);

    // 진행 중 이벤트
    List<Event> findNowEvents(String sort, int offset, int limit, List<String> regions);
    int countNowEvents(List<String> regions);

    // 완룓된 이벤트
    List<Event> findPastEvents(String sort, int offset, int limit, List<String> regions);
    int countPastEvents(List<String> regions);

    List<Event> findMyUpcomingEvents(Member member);
    List<Event> findMyNowEvents(Member member);
    List<Event> findMyPastEvents(Member member);

    Page<Event> findVenueOngoingOrUpcomingEvents(Long venueId, Pageable pageable);
    Page<Event> findVenuePastEvents(Long venueId, Pageable pageable);


    int countVenueOngoingOrUpcomingEvents(Long venueId);

    int countVenuePastEvents(Long venueId);

    List<Event> findEventsByVenueOrderByPopularity(Long venueId, Pageable pageable);

    List<Event> findVenueEventsLatest(Long venueId, Pageable pageable);

    int countVenueEvents(Long venueId);
}
