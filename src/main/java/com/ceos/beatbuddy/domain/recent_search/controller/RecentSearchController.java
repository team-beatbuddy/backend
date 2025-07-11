package com.ceos.beatbuddy.domain.recent_search.controller;

import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/recent-searches")
public class RecentSearchController implements RecentSearchApiDocs {
    private final RecentSearchService recentSearchService;

    /**
     * 개발용
     * */
    @PostMapping("/save")
    public ResponseEntity<Void> saveRecentSearch(
            @RequestParam String searchType,
            @RequestParam String keyword
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        recentSearchService.saveRecentSearch(searchType, keyword, memberId);

        return ResponseEntity.ok(null);
    }

    @Override
    @GetMapping()
    public ResponseEntity<ResponseDTO<List<String>>> getRecentSearches(
            @RequestParam String searchType
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<String> keywords = recentSearchService.getRecentSearches(memberId, searchType);

        return ResponseEntity
                .status(SuccessCode.RECENT_SEARCH_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.RECENT_SEARCH_SUCCESS, keywords));
    }
}
