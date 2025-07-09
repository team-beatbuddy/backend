package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes + 1 WHERE p.id = :postId")
    void increaseLike(@Param("postId") Long postId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes - 1 WHERE p.id = :postId")
    void decreaseLike(@Param("postId") Long postId);

    @Query("select f.hashtag from FreePost f where f.id = :id")
    List<FixedHashtag> findHashtagsByPostId(@Param("id") Long id);


    @Modifying
    @Query("UPDATE Post p SET p.scraps = p.scraps + 1 WHERE p.id = :postId")
    void increaseScrap(@Param("postId") Long postId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying
    @Query("UPDATE Post p SET p.scraps = p.scraps - 1 WHERE p.id = :postId")
    void decreaseScrap(@Param("postId") Long postId);

    @Modifying
    @Query("UPDATE Post p SET p.views = p.views + 1 WHERE p.id = :postId")
    void increaseViews(@Param("postId") Long postId);
}
