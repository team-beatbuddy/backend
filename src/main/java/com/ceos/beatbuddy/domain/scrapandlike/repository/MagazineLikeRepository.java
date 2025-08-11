package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MagazineLikeRepository extends JpaRepository<MagazineLike, Long> {
    boolean existsByMember_IdAndMagazine_Id(Long memberId, Long magazineId);
    void deleteByMember_IdAndMagazine_Id(Long memberId, Long magazineId);

    @Query("SELECT ml.magazine.id FROM MagazineLike ml WHERE ml.member.id = :memberId")
    List<Long> findMagazineIdsByMemberId(@Param("memberId") Long memberId);

    List<MagazineLike> findAllByMember_IdAndMagazine_IdIn(Long memberId, List<Long> magazineIds);

    void deleteAllByMagazine_Id(Long magazineId);
}
