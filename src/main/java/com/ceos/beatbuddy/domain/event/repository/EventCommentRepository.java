package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.EventCommentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, EventCommentId> {
    @Query("SELECT COALESCE(MAX(c.id), 0) + 1 FROM EventComment c")
    Long getNextCommentGroupId();

    Optional<EventComment> findTopByIdOrderByLevelDesc(Long parentCommentId);
}
