package com.ceos.beatbuddy.domain.follow.repository;

import com.ceos.beatbuddy.domain.follow.entity.Follow;
import com.ceos.beatbuddy.domain.follow.entity.FollowId;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, FollowId> {
    boolean existsById(FollowId id);

    Optional<Follow> findById(FollowId id);

    List<Follow> findAllByFollower(Member follower);

    List<Follow> findAllByFollowing(Member following);
}
