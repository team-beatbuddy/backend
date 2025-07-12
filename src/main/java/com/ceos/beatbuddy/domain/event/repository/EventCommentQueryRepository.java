package com.ceos.beatbuddy.domain.event.repository;

import com.ceos.beatbuddy.domain.event.entity.Event;
import com.ceos.beatbuddy.domain.event.entity.EventComment;

import java.util.List;

public interface EventCommentQueryRepository {
    
    /**
     * 특정 이벤트의 댓글 조회 (차단된 멤버 제외)
     * @param event 이벤트
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 댓글 목록 (차단된 멤버 제외)
     */
    List<EventComment> findAllByEventExcludingBlocked(Event event, List<Long> blockedMemberIds);
}