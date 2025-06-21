package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes + 1 WHERE p.id = :postId")
    void increaseLike(@Param("postId") Long postId);


    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes - 1 WHERE p.id = :postId")
    void decreaseLike(@Param("postId") Long postId);
}
