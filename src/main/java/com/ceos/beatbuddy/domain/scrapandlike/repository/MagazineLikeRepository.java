package com.ceos.beatbuddy.domain.scrapandlike.repository;

import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineInteractionId;
import com.ceos.beatbuddy.domain.scrapandlike.entity.MagazineLike;
import com.ceos.beatbuddy.domain.scrapandlike.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MagazineLikeRepository extends JpaRepository<MagazineLike, MagazineInteractionId> {
    boolean existsById(MagazineInteractionId id);

    @Query("SELECT m.id.magazineId FROM MagazineLike m WHERE m.id.memberId = :memberId")
    List<Long> findMagazineIdsByMemberId(@Param("memberId") Long memberId);

    List<MagazineLike> findAllByMember_IdAndMagazine_IdIn(Long memberId, List<Long> magazineIds);

    void deleteAllByMagazine_Id(Long magazineId);
}
