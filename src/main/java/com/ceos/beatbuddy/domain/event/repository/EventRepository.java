package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventStatus;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByHost(Member member);

    @Modifying
    @Query("UPDATE Event e SET e.likes = e.likes + 1 WHERE e.id = :eventId")
    void increaseLike(@Param("eventId") Long eventId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying
    @Query("UPDATE Event e SET e.likes = e.likes -1 WHERE e.id = :eventId")
    void decreaseLike(@Param("eventId") Long eventId);

    @Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id = :eventId")
    void increaseViews(@Param("eventId") Long eventId);

    List<Event> findByVenue(Venue venue);

    @Modifying
    @Query("UPDATE Event e SET e.status = 'PAST' WHERE e.status = 'NOW' AND e.endDate < :now")
    void updateToPast(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE Event e SET e.status = 'NOW' WHERE e.status = 'UPCOMING' AND e.startDate <= :now AND e.endDate > :now")
    void updateToNow(@Param("now") LocalDateTime now);
}
