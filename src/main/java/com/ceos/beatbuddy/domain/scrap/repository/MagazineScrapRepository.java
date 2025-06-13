package com.ceos.beatbuddy.domain.scrap.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrap.entity.MagazineScrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MagazineScrapRepository extends JpaRepository<MagazineScrap, Long> {
    List<MagazineScrap> findMagazineScrapByMember(Member member);
}
