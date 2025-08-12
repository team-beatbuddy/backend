package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventAttendance;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventAttendanceRepository extends JpaRepository<EventAttendance, Long> {
    boolean existsByMemberIdAndEventId(Long memberId, Long eventId);
    EventAttendance findByMemberIdAndEventId(Long memberId, Long eventId);
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

    // 특정 이벤트의 모든 참석자 정보 삭제
    @Modifying
    @Query("DELETE FROM EventAttendance ea WHERE ea.event = :event")
    void deleteByEvent(@Param("event") Event event);

}
