package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.PiecePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PiecePostRepository extends JpaRepository<PiecePost, Long>{
    /**
 * 지정된 회원 ID에 해당하는 게시글을 페이지 단위로 조회합니다.
 *
 * @param memberId 게시글을 조회할 회원의 ID
 * @param pageable 페이지 및 정렬 정보를 포함하는 객체
 * @return 회원 ID에 해당하는 게시글의 페이지
 */
Page<Post> findByMemberId(Long memberId, Pageable pageable);
}
