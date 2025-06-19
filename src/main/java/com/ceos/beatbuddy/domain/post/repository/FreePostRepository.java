package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FreePostRepository extends JpaRepository<FreePost, Long> {

    /**
 * 지정된 회원 ID에 해당하는 게시글을 페이지 단위로 조회합니다.
 *
 * @param memberId 게시글을 조회할 회원의 ID
 * @param pageable 페이지 정보 및 정렬 기준
 * @return 회원 ID에 해당하는 게시글의 페이지 객체
 */
Page<Post> findByMemberId(Long memberId, Pageable pageable);

}
