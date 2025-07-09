package com.ceos.beatbuddy.domain.comment.repository;

import com.ceos.beatbuddy.domain.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findAllByPost_Id(Long postId, Pageable pageable);

    // 단건 존재 여부
    boolean existsByPost_IdAndMember_Id(Long postId, Long memberId);

    // 최적화용 bulk 조회
    List<Comment> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);
}
