package com.ceos.beatbuddy.domain.member.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.entity.MemberGenre;
import com.ceos.beatbuddy.domain.member.entity.MemberMood;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberGenreRepository extends JpaRepository<MemberGenre, Long> {
    @Query("SELECT mg FROM MemberGenre mg WHERE mg.member = :member ORDER BY mg.createdAt DESC LIMIT 1")
    Optional<MemberGenre> findLatestGenreByMember(@Param("member") Member member);

    @Query("SELECT mg FROM MemberGenre mg WHERE mg.member = :member ORDER BY mg.createdAt DESC")
    List<MemberGenre> findAllByMember(@Param("member") Member member);

    @Query("SELECT m.id FROM MemberGenre m WHERE m.member = :member")
    List<Long> findIdsByMember(Member member);

    boolean existsByMember(Member member);

    void deleteByMember(Member member);
}
