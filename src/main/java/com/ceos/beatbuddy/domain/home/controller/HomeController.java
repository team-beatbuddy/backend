package com.ceos.beatbuddy.domain.home.controller;

import com.ceos.beatbuddy.domain.home.application.HomeService;
import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.application.RecommendService;
import com.ceos.beatbuddy.domain.venue.dto.RecommendFilterDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/home")
@Tag(name = "Home Controller", description = "홈에 있는 기능\n")
public class HomeController implements HomeApiDocs{
    private final HomeService homeService;
    private final MemberService memberService;
    private final RecommendService recommendService;

    @Override
    @GetMapping("/venue-for-me/keyword")
    public ResponseEntity<ResponseDTO<List<String>>> getMyKeyword() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        System.out.println(memberId);

        List<String> result = memberService.getPreferences(memberId);
        //KeywordResponseDTO result = homeService.getKeyword(memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MY_KEYWORD.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MY_KEYWORD, result));
    }

    @Override
    @GetMapping("/recommend-venue")
    public ResponseEntity<ResponseDTO<List<VenueResponseDTO>>> recommendVenues() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        System.out.println(memberId);

        List<VenueResponseDTO> result = homeService.saveArchiveAndRecommendVenues(memberId, 5L);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE, result));
    }

    @Override
    @PostMapping("/recommend-venue/filter")
    public ResponseEntity<ResponseDTO<List<VenueResponseDTO>>> recommendVenuesByFilter(@RequestBody RecommendFilterDTO recommendFilterDTO) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        System.out.println(memberId);

        List<VenueResponseDTO> result = recommendService.recommendVenuesByFilter(memberId, 5L, recommendFilterDTO);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE_AND_FILTER.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_RECOMMEND_WITH_FAVORITE_AND_FILTER, result));
    }
}
