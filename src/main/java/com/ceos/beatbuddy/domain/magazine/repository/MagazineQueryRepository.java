package com.ceos.beatbuddy.domain.magazine.repository;

import com.ceos.beatbuddy.domain.magazine.entity.Magazine;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MagazineQueryRepository {
    // isPinned 되고 isVisible 된 게시물들을 최신순으로 조회
    List<Magazine> findPinnedMagazines();

    List<Magazine> findAllVisibleMagazines(Pageable pageable);

    int countAllVisibleMagazines();
}
