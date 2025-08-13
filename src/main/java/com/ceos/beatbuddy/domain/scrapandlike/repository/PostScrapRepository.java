package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.post.entity.Post;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostScrap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostScrapRepository extends JpaRepository<PostScrap, Long> {
    boolean existsByMember_IdAndPost_Id(Long memberId, Long postId);
    @Modifying
    int deleteByMember_IdAndPost_Id(Long memberId, Long postId);

    @Query("SELECT ps.post FROM PostScrap ps WHERE ps.member.id = :memberId")
    Page<Post> findPostsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    List<PostScrap> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);
}
