package com.ceos.beatbuddy.domain.post.repository;

import com.ceos.beatbuddy.domain.post.entity.FixedHashtag;
import com.ceos.beatbuddy.domain.post.entity.FreePost;
import com.ceos.beatbuddy.domain.post.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes + 1 WHERE p.id = :postId")
    void increaseLike(@Param("postId") Long postId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying
    @Query("UPDATE Post p SET p.likes = p.likes - 1 WHERE p.id = :postId")
    void decreaseLike(@Param("postId") Long postId);

    @Query("select f from FreePost f where f.id = :id")
    Optional<FreePost> findFreePostById(@Param("id") Long id);
    
    default List<FixedHashtag> findHashtagsByPostId(Long id) {
        return findFreePostById(id)
                .map(FreePost::getHashtag)
                .orElse(List.of());
    }


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
    
    // 동시성 제어를 위한 PESSIMISTIC_WRITE 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Post p WHERE p.id = :id")
    Optional<Post> findByIdForUpdate(@Param("id") Long id);
}
