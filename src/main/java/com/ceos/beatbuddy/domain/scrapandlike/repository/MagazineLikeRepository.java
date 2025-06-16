package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MagazineLikeRepository extends JpaRepository<MagazineLike, Long> {
    boolean existsByMemberAndMagazine(Member member, Magazine magazine);
}
