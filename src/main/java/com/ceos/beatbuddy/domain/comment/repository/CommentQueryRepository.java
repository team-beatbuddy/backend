package com.ceos.beatbuddy.domain.comment.repository;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentQueryRepository {
    
    /**
     * 특정 포스트의 댓글 조회 (차단된 멤버 제외)
     * @param postId 포스트 ID
     * @param pageable 페이징 정보
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 댓글 페이지 (차단된 멤버 제외)
     */
    Page<Comment> findAllByPostIdExcludingBlocked(Long postId, Pageable pageable, List<Long> blockedMemberIds);
    
    /**
     * 멤버별 댓글 조회 (차단된 멤버 제외)
     * @param memberId 멤버 ID
     * @param postIds 포스트 ID 목록
     * @param blockedMemberIds 차단된 멤버 ID 목록
     * @return 댓글 목록 (차단된 멤버 제외)
     */
    List<Comment> findAllByMemberIdAndPostIdInExcludingBlocked(Long memberId, List<Long> postIds, List<Long> blockedMemberIds);
}