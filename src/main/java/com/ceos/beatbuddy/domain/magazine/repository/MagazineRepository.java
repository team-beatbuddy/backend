package com.ceos.beatbuddy.domain.magazine.repository;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MagazineRepository extends JpaRepository<Magazine, Long> {
    List<Magazine> findMagazinesByIsVisibleTrue();
    Optional<Magazine> findByIdAndIsVisibleTrue(Long id);
    @Modifying
    @Query("UPDATE Magazine m SET m.likes = m.likes + 1 WHERE m.id = :magazineId")
    void increaseLike(@Param("magazineId") Long magazineId);

    // 좋아요를 누른 것이 확인되어야만 좋아요 취소가 가능
    @Modifying
    @Query("UPDATE Magazine m SET m.likes = CASE WHEN m.likes > 0 THEN m.likes - 1 ELSE 0 END WHERE m.id = :magazineId")
    void decreaseLike(@Param("magazineId") Long magazineId);

    boolean existsByIsPinnedTrueAndOrderInHome(int orderInHome);
    boolean existsByIsPinnedTrueAndOrderInHomeAndIdNot(int orderInHome, Long excludingId);

    @Modifying
    @Query("UPDATE Magazine m SET m.views = m.views + 1 WHERE m.id = :magazineId")
    void increaseViews(@Param("magazineId") Long magazineId);
}
