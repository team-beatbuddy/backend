package com.ceos.beatbuddy.domain.recent_search.application;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.recent_search.entity.RecentSearch;
import com.ceos.beatbuddy.domain.recent_search.entity.SearchTypeEnum;
import com.ceos.beatbuddy.domain.recent_search.repository.RecentSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RecentSearchService {
    private final RecentSearchRepository recentSearchRepository;
    private final MemberService memberService;

    @Transactional
    public void saveRecentSearch(String rawType, String keyword, Long memberId) {
        Member member = memberService.validateAndGetMember(memberId);

        SearchTypeEnum type = SearchTypeEnum.from(rawType); // 유효성 검사 포함

        Optional<RecentSearch> existing = recentSearchRepository.findByMemberAndKeywordAndSearchType(member, keyword, type);

        if (existing.isPresent()) {
            existing.get().updateTheUpdatedAt(LocalDateTime.now());
        } else {
            recentSearchRepository.save(RecentSearch.builder()
                    .member(member)
                    .keyword(keyword)
                    .searchType(type)
                    .build());
        }

        // 10개 초과 시 오래된 것 제거
        List<RecentSearch> all = recentSearchRepository.findTop10ByMemberAndSearchTypeOrderByUpdatedAtDesc(member, type);
        if (all.size() > 10) {
            List<RecentSearch> toDelete = all.subList(10, all.size());
            recentSearchRepository.deleteAll(toDelete);
        }
    }

    public List<String> getRecentSearches(Long memberId, String rawType) {
        Member member = memberService.validateAndGetMember(memberId);
        SearchTypeEnum type = SearchTypeEnum.from(rawType); // 문자열 → enum 변환

        List<RecentSearch> recentSearches =
                recentSearchRepository.findTop10ByMemberAndSearchTypeOrderByUpdatedAtDesc(member, type);

        return recentSearches.stream()
                .map(RecentSearch::getKeyword)
                .toList();
    }
}
