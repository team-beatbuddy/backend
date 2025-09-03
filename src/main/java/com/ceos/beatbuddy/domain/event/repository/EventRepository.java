package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByHost(Member member);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.likes = e.likes + 1 WHERE e.id = :eventId")
    void increaseLike(@Param("eventId") Long eventId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.likes = CASE WHEN e.likes > 0 THEN e.likes - 1 ELSE 0 END WHERE e.id = :eventId")
    void decreaseLike(@Param("eventId") Long eventId);
    
    // 좋아요 수를 지정된 개수만큼 감소
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.likes = CASE WHEN e.likes >= :count THEN e.likes - :count ELSE 0 END WHERE e.id = :eventId")
    void decreaseLike(@Param("eventId") Long eventId, @Param("count") int count);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Event e SET e.views = e.views + 1 WHERE e.id = :eventId")
    void increaseViews(@Param("eventId") Long eventId);

    int countAllByVenue_Id(Long venueId);

    // 오늘 시작 이전이면 이미 과거로 간 것: NOW → PAST
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Event e
           set e.status = 'PAST'
         where e.status = 'NOW'
           and e.endDate < :startOfToday
    """)
    int updateToPast(@Param("startOfToday") LocalDateTime startOfToday);

    // 현재 날짜가 이벤트 기간 내에 있으면: UPCOMING → NOW
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Event e
           set e.status = 'NOW'
         where e.status = 'UPCOMING'
           and e.startDate <= :startOfToday
           and e.endDate   >= :startOfToday
    """)
    int updateToNow(@Param("startOfToday") LocalDateTime startOfToday);

    // 종료일이 오늘 시작보다 이전이면: UPCOMING → PAST (이상치 정리)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Event e
           set e.status = 'PAST'
         where e.status = 'UPCOMING'
           and e.endDate < :startOfToday
    """)
    int updateUpcomingToPast(@Param("startOfToday") LocalDateTime startOfToday);
    // 동시성 제어를 위한 PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
