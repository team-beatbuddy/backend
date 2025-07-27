package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.CommentLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentLikeRepository extends JpaRepository<CommentLike, Long> {

    // 댓글 좋아요 여부 확인
    boolean existsByComment_IdAndMember_Id(Long commentId, Long memberId);

    // 댓글 좋아요 삭제
    void deleteByComment_IdAndMember_Id(Long commentId, Long memberId);
}
