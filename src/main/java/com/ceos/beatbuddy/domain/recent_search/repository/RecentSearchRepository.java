package com.ceos.beatbuddy.domain.recent_search.repository;

import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.recent_search.entity.RecentSearch;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RecentSearchRepository extends JpaRepository<RecentSearch, Long> {
    Optional<RecentSearch> findByMemberAndKeywordAndSearchType(Member member, String keyword, SearchTypeEnum type);

    List<RecentSearch> findTop10ByMemberAndSearchTypeOrderByUpdatedAtDesc(Member member, SearchTypeEnum type);

    List<RecentSearch> findByMemberAndSearchTypeOrderByUpdatedAtDesc(Member member, SearchTypeEnum type);
}
