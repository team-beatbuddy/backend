package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.EventComment;
import com.ceos.beatbuddy.domain.event.entity.EventCommentId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, EventCommentId> {
    @Query("SELECT COALESCE(MAX(c.id), 0) + 1 FROM EventComment c")
    Long getNextCommentGroupId();

    Optional<EventComment> findTopByIdOrderByLevelDesc(Long parentCommentId);

    // 댓글 삭제 (전체 삭제 대댓글도)
    @Modifying
    @Query("DELETE FROM EventComment c WHERE c.id = :id")
    void deleteAllById(@Param("id") Long id);

    // 해당 댓글만 삭제
    @Modifying
    @Query("DELETE FROM EventComment c WHERE c.id = :id AND c.level = :level")
    void deleteByIdAndLevel(@Param("id") Long id, @Param("level") Integer level);
}
