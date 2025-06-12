package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
//    Optional<Member> findById(Long Id);
    Boolean existsDistinctByNickname(String nickname);
    Optional<Member> findByNickname(String nickname);
    Boolean existsRegionsById(Long memberId);

    boolean existsByLoginId(String id);
}
