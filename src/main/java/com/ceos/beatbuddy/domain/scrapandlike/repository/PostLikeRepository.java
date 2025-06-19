package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.PostInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostLikeRepository extends JpaRepository<PostLike, PostInteractionId> {
    boolean existsById(PostInteractionId id);
    void deleteById(PostInteractionId id);
    List<PostLike> findAllByMember_IdAndPost_IdIn(Long memberId, List<Long> postIds);
}
