package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MemberBlockRepository extends JpaRepository<MemberBlock, Long> {
    
    /**
     * 특정 사용자가 차단한 멤버들의 ID 목록을 조회
     */
    @Query("SELECT mb.blocked.id FROM MemberBlock mb WHERE mb.blocker.id = :blockerId")
    Set<Long> findBlockedMemberIdsByBlockerId(@Param("blockerId") Long blockerId);

    /**
     * 두 사용자 간의 차단 관계가 존재하는지 확인
     */
    @Query("SELECT mb FROM MemberBlock mb WHERE mb.blocker.id = :blockerId AND mb.blocked.id = :blockedId")
    Optional<MemberBlock> findByBlockerIdAndBlockedId(@Param("blockerId") Long blockerId, @Param("blockedId") Long blockedId);
    
    /**
     * 특정 사용자가 차단당한 횟수 조회
     */
    @Query("SELECT COUNT(mb) FROM MemberBlock mb WHERE mb.blocked.id = :blockedId")
    Long countByBlockedId(@Param("blockedId") Long blockedId);
}