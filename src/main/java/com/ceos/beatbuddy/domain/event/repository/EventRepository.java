package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByHost(Member member);

    @Modifying
    @Query("UPDATE Event e SET e.likes = e.likes + 1 WHERE e.id = :eventId")
    void increaseLike(@Param("eventId") Long eventId);


    @Modifying
    @Query("UPDATE Event e SET e.likes = e.likes -1 WHERE e.id = :eventId")
    void decreaseLike(@Param("eventId") Long eventId);
}
