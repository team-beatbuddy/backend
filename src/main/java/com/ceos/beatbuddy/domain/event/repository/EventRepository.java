package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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

    @Query("SELECT FUNCTION('DATE_FORMAT', e.endDate, '%Y-%m'), e " +
            "FROM Event e " +
            "WHERE e.endDate BETWEEN :from AND :to " +
            "ORDER BY FUNCTION('DATE_FORMAT', e.endDate, '%Y-%m') DESC, e.likes DESC")
    List<Object[]> findPastEventsGroupedByMonthOptimized(@Param("from") LocalDate from,
                                                         @Param("to") LocalDate to);


    @Modifying
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id = :eventId")
    void increaseViews(@Param("eventId") Long eventId);
}
