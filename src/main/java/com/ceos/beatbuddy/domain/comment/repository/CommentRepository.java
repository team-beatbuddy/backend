package com.ceos.beatbuddy.domain.comment.repository;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPost_Id(Long postId, Pageable pageable);

    // 단건 존재 여부
    boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

    // 최적화용 bulk 조회
    List<Comment> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);

    List<Comment> findAllByPost_IdOrderByCreatedAtAsc(Long postId);

    @Modifying
    @Query("UPDATE Comment c SET c.likes = CASE WHEN c.likes > 0 THEN c.likes - 1 ELSE 0 END WHERE c.id = :commentId")
    void decreaseLikesById(Long commentId);

    @Modifying
    @Query("UPDATE Comment c SET c.likes = c.likes + 1 WHERE c.id = :commentId")
    void increaseLikesById(Long commentId);

    boolean existsByReplyId(Long parentCommentId);
    
    // 특정 포스트에서 특정 멤버의 기존 익명 닉네임 찾기 (첫 번째 결과만)
    Optional<Comment> findTopByPost_IdAndMember_IdAndIsAnonymousTrueAndAnonymousNicknameIsNotNullOrderByCreatedAtAsc(Long postId, Long memberId);
    
    // 특정 포스트의 모든 익명 닉네임 조회 (중복 제거)
    @Query("SELECT DISTINCT c.anonymousNickname FROM Comment c WHERE c.post.id = :postId AND c.isAnonymous = true AND c.anonymousNickname IS NOT NULL ORDER BY c.anonymousNickname")
    List<String> findDistinctAnonymousNicknamesByPost_Id(Long postId);
    
    // 특정 포스트의 모든 댓글 삭제
    @Modifying
    void deleteByPost_Id(Long postId);
}
