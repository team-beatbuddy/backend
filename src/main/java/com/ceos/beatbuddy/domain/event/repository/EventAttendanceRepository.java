package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.event.entity.EventAttendanceId;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, EventAttendanceId> {
    boolean existsById(EventAttendanceId id);
    List<EventAttendance> findAllByEventId(Long eventId);

    List<EventAttendance> findByMember(Member member);

    @Query("""
    select ea.event
    from EventAttendance ea
    where ea.member = :member
      and ea.event.startDate >= :from
      and ea.event.startDate < :to
""")
    List<Event> findEventsByMemberAndStartDateBetween(
            @Param("member") Member member,
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to);

}
