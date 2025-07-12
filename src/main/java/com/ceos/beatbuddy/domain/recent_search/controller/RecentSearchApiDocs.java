package com.ceos.beatbuddy.domain.recent_search.controller;

import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @ApiResponse(
            responseCode = "404", description = "리소스 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "존재하지 않는 유저",
                            value = SwaggerExamples.MEMBER_NOT_EXIST)
            )
    )
    ResponseEntity<ResponseDTO<List<String>>> getRecentSearches(
            @RequestParam @Pattern(regexp = "^(EVENT|VENUE|FREE_POST)$",
                    message = "검색 타입은 EVENT, VENUE, FREE_POST 중 하나여야 합니다")
            @Schema(description = "검색 타입", allowableValues = {"EVENT", "VENUE", "FREE_POST"})
            String searchType
    );

    @Operation(summary = "최근 검색어 삭제", description = """
        최근 검색어를 삭제합니다. 검색 타입에 따라 이벤트, 장소, 자유 게시글의 최근 검색어를 삭제합니다.
        - 검색 타입은 `EVENT`, `VENUE`, `FREE_POST` 중 하나여야 합니다.
        - 검색 타입이 잘못된 경우 `400 Bad Request` 에러가 발생합니다.
        - 삭제할 검색어가 존재하지 않는 경우 `404 Not Found` 에러가 발생합니다.
            """)

    @ApiResponse(responseCode = "200", description = "최근 검색어 삭제 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "최근 검색어 삭제 성공 예시",
                            value = """
                                {
                                    "status": 200,
                                    "code": "RECENT_SEARCH_DELETE_SUCCESS",
                                    "message": "최근 검색어를 성공적으로 삭제했습니다.",
                                    "data": "최근 검색어를 성공적으로 삭제했습니다."
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
    @ApiResponse(
            responseCode = "404", description = "리소스 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = {@ExampleObject(
                            name = "존재하지 않는 검색어",
                            value = SwaggerExamples.RECENT_SEARCH_NOT_FOUND),
                            @ExampleObject(
                                    name = "존재하지 않는 유저",
                                    value = SwaggerExamples.MEMBER_NOT_EXIST)}
            )
    )
    ResponseEntity<ResponseDTO<String>> deleteRecentSearch(
            @RequestParam @Pattern(regexp = "^(EVENT|VENUE|FREE_POST)$",
                    message = "검색 타입은 EVENT, VENUE, FREE_POST 중 하나여야 합니다")
            @Schema(description = "검색 타입", allowableValues = {"EVENT", "VENUE", "FREE_POST"})
            String searchType,
            @RequestParam @NotNull(message = "삭제하고자 하는 키워드는 비어있을 수 없습니다.") String keyword
    );
}
