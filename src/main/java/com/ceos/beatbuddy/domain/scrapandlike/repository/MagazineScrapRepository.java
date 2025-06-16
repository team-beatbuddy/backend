package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineScrap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface MagazineScrapRepository extends JpaRepository<MagazineScrap, MagazineInteractionId> {
    List<MagazineScrap> findAllByMember(Member member);
    boolean existsById(MagazineInteractionId id);
}
