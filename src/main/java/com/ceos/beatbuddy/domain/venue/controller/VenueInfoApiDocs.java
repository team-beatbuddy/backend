package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.dto.VenueCouponResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

public interface VenueInfoApiDocs {
    /**
     * 베뉴 ID로 쿠폰 목록 조회
     *
     * @param venueId 조회할 베뉴의 ID
     * @return 베뉴에 해당하는 쿠폰 목록
     */
    @Operation(summary = "베뉴 ID로 쿠폰 목록 조회",
            description = "특정 베뉴에 해당하는 유효한 쿠폰 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "쿠폰 목록 조회 성공",
    content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @ExampleObject(name = "조회 성공", value = """
                {
                  "status": 200,
                  "code": "SUCCESS_GET_VENUE_COUPONS",
                  "message": "베뉴 쿠폰 목록을 성공적으로 조회했습니다.",
                  "data": [
                    {
                      "couponId": 1,
                      "name": "1번 테스트",
                      "expireDate": "2025-07-13",
                      "quota": 1,
                      "remaining": 0,
                      "policy": "WEEKLY",
                      "howToUse": "string",
                      "notes": "string",
                      "soldOut": true
                    },
                    {
                      "couponId": 2,
                      "name": "1번 테스트",
                      "expireDate": "2025-07-13",
                      "quota": 30,
                      "remaining": 0,
                      "policy": "WEEKLY",
                      "howToUse": "string",
                      "notes": "string",
                      "soldOut": true
                    }
                  ]
                }""")))
    @ApiResponse(
            responseCode = "404",
            description = "베뉴가 존재하지 않음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "베뉴 없음", value = SwaggerExamples.VENUE_NOT_EXIST))
    )
    ResponseEntity<ResponseDTO<List<VenueCouponResponseDTO>>> getCouponsByVenue(@PathVariable Long venueId);
}
