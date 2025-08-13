package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMember_IdAndPost_Id(Long memberId, Long postId);
    @Modifying
    int deleteByMember_IdAndPost_Id(Long memberId, Long postId);
    List<PostLike> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);
}
