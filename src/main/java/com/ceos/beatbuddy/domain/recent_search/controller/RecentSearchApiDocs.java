package com.ceos.beatbuddy.domain.recent_search.controller;

import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecentSearchApiDocs {

    @Operation(summary = "최근 검색어 조회", description = """
        최근 검색어를 조회합니다. 검색 타입에 따라 이벤트, 장소, 자유 게시글의 최근 검색어를 가져옵니다.
        - 검색 타입은 `EVENT`, `VENUE`, `FREE_POST` 중 하나여야 합니다.
        - 검색 타입이 잘못된 경우 `400 Bad Request` 에러가 발생합니다.
        - 최근 검색어에 있는 검색어로 다시 검색하는 경우, 제일 처음으로 올라옵니다.
            """)
    @ApiResponse(responseCode = "200", description = "최근 검색어 조회 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "최근 검색어 조회 성공 예시",
                            value = """
                                {
                                  "status": 200,
                                  "code": "RECENT_SEARCH_SUCCESS",
                                  "message": "최근 검색어를 성공적으로 조회했습니다.",
                                  "data": [
                                    "이태원 클럽",
                                    "홍대"
                                  ]
                                }
                                    """)
            )
    )
    @ApiResponse(
            responseCode = "400", description = "잘못된 검색 타입",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "잘못된 검색 타입 예시",
                            value = SwaggerExamples.INVALID_SEARCH_TYPE)
            )
    )
    ResponseEntity<ResponseDTO<List<String>>> getRecentSearches(
            @RequestParam String searchType
    );
}
