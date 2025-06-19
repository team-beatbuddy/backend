package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PiecePostRepository extends JpaRepository<PiecePost, Long>{
    Page<Post> findByMemberId(Long memberId, Pageable pageable);
}
