package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 댓글 좋아요 여부 확인
    boolean existsByComment_IdAndMember_Id(Long commentId, Long memberId);

    // 댓글 좋아요 삭제 (특정 회원)
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM CommentLike cl WHERE cl.comment.id = :commentId AND cl.member.id = :memberId")
    int deleteByComment_IdAndMember_Id(Long commentId, Long memberId);
    
    // 댓글에 달린 모든 좋아요 삭제 (댓글 삭제 시 사용)
    void deleteByCommentId(Long commentId);
}
