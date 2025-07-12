package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
    List<EventComment> findAllByEvent(Event event);

    @Modifying
    @Query("DELETE FROM EventComment c WHERE c.parentId = :id OR c.id = :id")
    void deleteAllByParentIdOrId(Long id);

    @Query("SELECT ec FROM EventComment ec JOIN FETCH ec.author WHERE ec.event = :event")
    List<EventComment> findAllByEventWithAuthor(@Param("event") Event event);

}
