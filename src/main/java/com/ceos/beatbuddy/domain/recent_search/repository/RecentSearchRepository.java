package com.ceos.beatbuddy.domain.recent_search.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.recent_search.entity.RecentSearch;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {
    Optional<RecentSearch> findByMemberAndKeywordAndSearchType(Member member, String keyword, SearchTypeEnum type);

    List<RecentSearch> findTop10ByMemberAndSearchTypeOrderByUpdatedAtDesc(Member member, SearchTypeEnum type);
}
