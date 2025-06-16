package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MagazineScrapRepository extends JpaRepository<MagazineScrap, Long> {
    List<MagazineScrap> findAllByMember(Member member);
}
