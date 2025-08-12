package com.ceos.beatbuddy.domain.follow.repository;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    
    Optional<Follow> findByFollower_IdAndFollowing_Id(Long followerId, Long followingId);
    
    void deleteByFollower_IdAndFollowing_Id(Long followerId, Long followingId);

    @Query("SELECT f FROM Follow f JOIN FETCH f.following fm JOIN FETCH fm.postProfileInfo WHERE f.follower = :follower")
    List<Follow> findAllByFollower(@Param("follower") Member follower);

    @Query("SELECT f FROM Follow f JOIN FETCH f.follower fm JOIN FETCH fm.postProfileInfo WHERE f.following = :following")
    List<Follow> findAllByFollowing(@Param("following") Member following);
    
    // 차단된 사용자를 제외한 팔로잉 목록
    @Query("SELECT f FROM Follow f JOIN FETCH f.following fm JOIN FETCH fm.postProfileInfo " +
           "WHERE f.follower = :follower " +
           "AND f.following.id NOT IN :blockedMemberIds")
    List<Follow> findAllByFollowerExcludingBlocked(@Param("follower") Member follower, 
                                                   @Param("blockedMemberIds") Set<Long> blockedMemberIds);
    
    // 차단된 사용자를 제외한 팔로워 목록
    @Query("SELECT f FROM Follow f JOIN FETCH f.follower fm JOIN FETCH fm.postProfileInfo " +
           "WHERE f.following = :following " +
           "AND f.follower.id NOT IN :blockedMemberIds")
    List<Follow> findAllByFollowingExcludingBlocked(@Param("following") Member following, 
                                                    @Param("blockedMemberIds") Set<Long> blockedMemberIds);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :memberId")
    Set<Long> findFollowingMemberIds(@Param("memberId") Long memberId);

    boolean existsByFollowerIdAndFollowingId(Long memberId, Long id);
}
