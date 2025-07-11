package com.ceos.beatbuddy.domain.home.controller;


import com.ceos.beatbuddy.domain.heartbeat.dto.HeartbeatResponseDTO;
import com.ceos.beatbuddy.domain.member.application.RecommendService;
import com.ceos.beatbuddy.domain.search.dto.SearchQueryResponseDTO;
import com.ceos.beatbuddy.domain.search.dto.SearchRankResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.RecommendFilterDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
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
@RequestMapping("/recommend")
@RequiredArgsConstructor
@Tag(name = "Recommend Controller", description = "사용자의 장르와 분위기에 따른 베뉴를 추천하는 컨트롤러")
public class RecommendController implements RecommendApiDocs{

    private final RecommendService recommendService;

    @Override
    @GetMapping("")
    public ResponseEntity<ResponseDTO<List<VenueResponseDTO>>> recommendVenues() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<VenueResponseDTO> result = recommendService.recommendVenues(memberId, 5L);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE, result));
    }

    @PostMapping("/filter")
    public ResponseEntity<List<VenueResponseDTO>> recommendVenuesByFilter(@RequestBody RecommendFilterDTO recommendFilterDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(recommendService.recommendVenuesByFilter(memberId, 5L, recommendFilterDTO));
    }

//    @GetMapping("/genre")
//    @Operation(summary = "장르별 베뉴 추천\n",
//            description = "사용자의 장르 선호도에 의해 추출된 추천 베뉴를 조회합니다.\n"
//                    + "현재는 2개의 베뉴를 추천하도록 설정했습니다.(추후 변경 가능)")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "장르별 추천 베뉴를 조회하는데 성공했습니다."
//                    , content = @Content(mediaType = "application/json"
//                    , schema = @Schema(implementation = VenueResponseDTO.class))),
//            @ApiResponse(responseCode = "404", description = "\"요청한 유저가 존재하지 않습니다\" or \"유저의 장르 선호도가 존재하지 않습니다\"",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ResponseTemplate.class)))
//    })
//    public ResponseEntity<List<VenueResponseDTO>> recommendByGenre() {
//        Long memberId = SecurityUtils.getCurrentMemberId();
//        return ResponseEntity.ok(recommendService.recommendVenuesByGenre(memberId, 2L));
//    }
//
//    @GetMapping("/mood")
//    @Operation(summary = "분위기별 베뉴 추천",
//            description = "사용자의 분위기 선호도에 의해 추출된 추천 베뉴를 조회합니다.\n"
//                    + "현재는 2개의 베뉴를 추천하도록 설정했습니다.(추후 변경 가능)")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", description = "분위기별 추천 베뉴를 조회하는데 성공했습니다."
//                    , content = @Content(mediaType = "application/json"
//                    , schema = @Schema(implementation = VenueResponseDTO.class))),
//            @ApiResponse(responseCode = "404", description = "\"요청한 유저가 존재하지 않습니다\" or \"유저의 분위기 선호도가 존재하지 않습니다\"",
//                    content = @Content(mediaType = "application/json",
//                            schema = @Schema(implementation = ResponseTemplate.class)))
//    })
//    public ResponseEntity<List<VenueResponseDTO>> recommendByMood() {
//        Long memberId = SecurityUtils.getCurrentMemberId();
//        return ResponseEntity.ok(recommendService.recommendVenuesByMood(memberId, 2L));
//    }



}
