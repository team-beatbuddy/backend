package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByHost(Member member);
}
