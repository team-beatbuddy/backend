package com.ceos.beatbuddy.domain.follow.repository;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.follow.entity.FollowId;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsById(FollowId id);

    Optional<Follow> findById(FollowId id);

    List<Follow> findAllByFollower(Member follower);

    List<Follow> findAllByFollowing(Member following);

    @Query("SELECT f.following.id FROM Follow f WHERE f.follower.id = :memberId")
    Set<Long> findFollowingMemberIds(@Param("memberId") Long memberId);

    boolean existsByFollowerIdAndFollowingId(Long memberId, Long id);
}
