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
import java.util.Optional;

@Repository
public interface EventCommentRepository extends JpaRepository<EventComment, Long> {
    List<EventComment> findAllByEvent(Event event);

    @Modifying
    @Query("DELETE FROM EventComment c WHERE c.parentId = :id OR c.id = :id")
    void deleteAllByParentIdOrId(Long id);

    @Query("SELECT ec FROM EventComment ec JOIN FETCH ec.author WHERE ec.event = :event")
    List<EventComment> findAllByEventWithAuthor(@Param("event") Event event);

    // 이벤트의 특정 멤버가 작성한 첫 번째 익명 댓글 조회
    Optional<EventComment> findTopByEvent_IdAndAuthor_IdAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(Long eventId, Long authorId);
    
    // 이벤트에서 사용된 모든 익명 닉네임 조회
    @Query("SELECT DISTINCT ec.anonymousNickname FROM EventComment ec WHERE ec.event.id = :eventId AND ec.anonymousNickname IS NOT NULL")
    List<String> findDistinctAnonymousNicknamesByEventId(@Param("eventId") Long eventId);

    // 특정 이벤트의 모든 댓글 삭제
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM EventComment ec WHERE ec.event = :event")
    int deleteByEvent(@Param("event") Event event);

}
