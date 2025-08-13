package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {
    boolean existsByMember_IdAndPost_Id(Long memberId, Long postId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM PostLike pl WHERE pl.member.id = :memberId AND pl.post.id = :postId")
    int deleteByMember_IdAndPost_Id(Long memberId, Long postId);
    List<PostLike> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);
}
