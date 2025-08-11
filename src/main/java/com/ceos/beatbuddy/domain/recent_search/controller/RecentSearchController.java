package com.ceos.beatbuddy.domain.recent_search.controller;

import com.ceos.beatbuddy.domain.recent_search.application.RecentSearchService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
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
    @Profile("dev")
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
            @RequestParam @Pattern(regexp = "^(EVENT|VENUE|FREE_POST)$",
                    message = "검색 타입은 EVENT, VENUE, FREE_POST 중 하나여야 합니다")
            @Schema(description = "검색 타입", allowableValues = {"EVENT", "VENUE", "FREE_POST"})
            String searchType
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<String> keywords = recentSearchService.getRecentSearches(memberId, searchType);

        return ResponseEntity
                .status(SuccessCode.RECENT_SEARCH_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.RECENT_SEARCH_SUCCESS, keywords));
    }

    @Override
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO<String>> deleteRecentSearch(
            @RequestParam @Pattern(regexp = "^(EVENT|VENUE|FREE_POST)$",
                    message = "검색 타입은 EVENT, VENUE, FREE_POST 중 하나여야 합니다")
            @Schema(description = "검색 타입", allowableValues = {"EVENT", "VENUE", "FREE_POST"})
            String searchType,
            @RequestParam @NotNull(message = "삭제하고자 하는 키워드는 비어있을 수 없습니다.") String keyword
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        recentSearchService.deleteRecentSearch(memberId, keyword, searchType);

        return ResponseEntity
                .status(SuccessCode.RECENT_SEARCH_DELETE_SUCCESS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.RECENT_SEARCH_DELETE_SUCCESS, "최근 검색어를 성공적으로 삭제했습니다."));
    }
}
