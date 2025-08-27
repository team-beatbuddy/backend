package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByLoginId(String loginId);
//    Optional<Member> findById(Long Id);
    Boolean existsDistinctByNickname(String nickname);
    Optional<Member> findByNickname(String nickname);
    Boolean existsRegionsById(Long memberId);
    
    Boolean existsByPostProfileInfo_PostProfileNickname(String postProfileNickname);

    boolean existsByLoginId(String id);

    List<Member> findAllByRole(Role role);

    List<Member> findAllByFcmTokenIsNotNull();

}
