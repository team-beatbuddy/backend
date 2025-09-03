package com.ceos.beatbuddy.domain.search.controller;

import com.ceos.beatbuddy.domain.search.application.SearchService;
import com.ceos.beatbuddy.domain.search.dto.SearchDropDownDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchMapRequestDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchPageResponseDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchRankResponseDTO;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
@Tag(name = "Search Controller", description = "검색 컨트롤러\n"
        + "사용자가 검색바에 검색하는 기능, 실시간 검색어 차트 조회 기능이 있습니다.")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/rank")
    @Operation(summary = "검색어 TOP10 차트 기능", description = "실시간 검색량 내림차순 검색어 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "검색어 TOP10 차트 조회 성공"
                    , content = @Content(mediaType = "application/json"
                    , array = @ArraySchema(schema = @Schema(implementation = SearchRankResponseDTO.class))))
    })
    public List<SearchRankResponseDTO> searchRankList(){
        return searchService.searchRankList();
    }

    @PostMapping("/home/drop-down")
    @Operation(summary = "홈 검색 드롭다운 기능", description = """
            사용자가 검색바에 입력한 검색어로 검색한 결과에서 드롭다운으로 필터링/정렬한 베뉴 조회
            - keyword, regionTag, genreTag는 필수가 아닙니다.
            - sortCriteria는 필수입니다. (인기순, 가까운 순)
            - 가까운 순으로 정렬하고 싶다면 latitude, longitude를 보내주셔야 합니다.
            - keyword는 그대로 보내주면 됩니다.
            - regionTag는 (홍대, 압구정, 강남/신사, 이태원, 기타)로 정확하게 보내주셔야 합니다.
            - genreTag는 (HIPHOP,R&B,EDM,HOUSE,TECHNO,SOUL&FUNK,ROCK,LATIN,K-POP,POP)로 정확하게 보내주셔야 합니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "드롭다운 필터링 베뉴 조회 성공"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = SearchPageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "검색어가 입력되지 않아서 검색 실패 or 리스트에 없는 장르명이나 지역명 입력 시 에러 반환"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다 or 베뉴 장르가 존재하지 않습니다"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<SearchPageResponseDTO> searchDropDownHome(@RequestParam(required = false) 
                                                                           @Schema(description = "지역 필터 값", allowableValues = {"홍대", "압구정", "강남/신사", "이태원", "기타"}) String regionTag,
                                                                           @RequestParam(required = false) 
                                                                           @Schema(description = "장르 필터 값", allowableValues = {"HIPHOP", "R&B", "EDM", "HOUSE", "TECHNO", "SOUL&FUNK", "ROCK", "LATIN", "K-POP", "POP"}) String genreTag,
                                                                           @RequestParam(required = false, defaultValue = "가까운 순") 
                                                                           @Schema(description = "정렬 기준", allowableValues = {"인기순", "가까운 순"}) String criteria,
                                                                           @RequestParam(required = false) String keyword,
                                                                           @RequestParam(required = false) Double latitude,
                                                                           @RequestParam(required = false) Double longitude,
                                                                           @RequestParam(required = false, defaultValue = "1") int page,
                                                                           @RequestParam(required = false, defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        SearchDropDownDTO searchDropDownDTO = SearchDropDownDTO.builder()
                .keyword(keyword)
                .regionTag(regionTag)
                .genreTag(genreTag)
                .sortCriteria(criteria)
                .build();
        return ResponseEntity.ok(searchService.searchDropDown(memberId, searchDropDownDTO, latitude, longitude, "HOME", page, size));
    }

    @PostMapping("/map/drop-down")
    @Operation(summary = "지도 드롭다운 기능", description = """
            사용자가 검색바에 입력한 검색어로 검색한 결과에서 드롭다운으로 필터링/정렬한 베뉴 조회
            - keyword, regionTag, genreTag는 필수가 아닙니다.
            - sortCriteria는 필수입니다. (인기순, 가까운 순)
            - 가까운 순으로 정렬하고 싶다면 latitude, longitude를 보내주셔야 합니다.
            - keyword는 그대로 보내주면 됩니다.
            - regionTag는 (홍대, 압구정, 강남/신사, 이태원, 기타)로 정확하게 보내주셔야 합니다.
            - genreTag는 (HIPHOP,R&B,EDM,HOUSE,TECHNO,SOUL&FUNK,ROCK,LATIN,K-POP,POP)로 정확하게 보내주셔야 합니다.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "드롭다운 필터링 베뉴 조회 성공"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = SearchPageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "검색어가 입력되지 않아서 검색 실패 or 리스트에 없는 장르명이나 지역명 입력 시 에러 반환"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다 or 베뉴 장르가 존재하지 않습니다"
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<SearchPageResponseDTO> searchDropDown(@RequestParam(required = false) 
                                                                @Schema(description = "지역 필터 값", allowableValues = {"홍대", "압구정", "강남/신사", "이태원", "기타"}) String regionTag,
                                                                @RequestParam(required = false) 
                                                                @Schema(description = "장르 필터 값", allowableValues = {"HIPHOP", "R&B", "EDM", "HOUSE", "TECHNO", "SOUL&FUNK", "ROCK", "LATIN", "K-POP", "POP"}) String genreTag,
                                                                @RequestParam(required = false, defaultValue = "가까운 순") 
                                                                @Schema(description = "정렬 기준", allowableValues = {"인기순", "가까운 순"}) String criteria,
                                                                @RequestParam(required = false) String keyword,
                                                                       @RequestParam(required = false) Double latitude,
                                                                       @RequestParam(required = false) Double longitude,
                                                                       @RequestParam(required = false, defaultValue = "1") int page,
                                                                       @RequestParam(required = false, defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        SearchDropDownDTO searchDropDownDTO = SearchDropDownDTO.builder()
                .keyword(keyword)
                .regionTag(regionTag)
                .genreTag(genreTag)
                .sortCriteria(criteria)
                .build();
        return ResponseEntity.ok(searchService.searchDropDown(memberId, searchDropDownDTO, latitude, longitude, "MAP", page, size));
    }

    @PostMapping("/map/search")
    @Operation(summary = "지도 베뉴 검색 (JSON Body)", description = """
            JSON body로 지도 베뉴 검색을 수행합니다.
            - sortCriteria는 필수입니다. (인기순, 가까운 순)
            - 가까운 순으로 정렬하고 싶다면 latitude, longitude를 보내주셔야 합니다.
            - regionTag는 (홍대, 압구정, 강남/신사, 이태원, 기타)로 정확하게 보내주셔야 합니다.
            - genreTag는 (HIPHOP,R&B,EDM,HOUSE,TECHNO,SOUL&FUNK,ROCK,LATIN,K-POP,POP)로 정확하게 보내주셔야 합니다.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "베뉴 검색 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SearchPageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "검색 요청이 올바르지 않음",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<SearchPageResponseDTO> searchMapWithBody(@RequestBody SearchMapRequestDTO request) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(searchService.searchDropDown(
                memberId, 
                request.toSearchDropDownDTO(), 
                request.getLatitude(), 
                request.getLongitude(), 
                "MAP", 
                request.getPage(),
                request.getSize()
        ));
    }
}
