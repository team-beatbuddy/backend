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

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.status = 'PAST' WHERE e.status = 'NOW' AND e.endDate < :now")
    int updateToPast(@Param("now") LocalDateTime now);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.status = 'NOW' WHERE e.status = 'UPCOMING' AND e.startDate <= :now AND e.endDate >= :now")
    int updateToNow(@Param("now") LocalDateTime now);
    
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE Event e SET e.status = 'PAST' WHERE e.status = 'UPCOMING' AND e.endDate < :now")
    int updateUpcomingToPast(@Param("now") LocalDateTime now);
    
    // 동시성 제어를 위한 PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdForUpdate(@Param("id") Long id);
}
