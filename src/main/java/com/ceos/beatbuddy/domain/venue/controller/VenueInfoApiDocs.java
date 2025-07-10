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
            description = """
    특정 베뉴에 해당하는 유효한 쿠폰 목록을 조회합니다.
    - 베뉴 ID를 통해 해당 베뉴의 쿠폰을 조회합니다.
    - 쿠폰은 유효한 것만 조회됩니다.
    - 내가 발급 받았는지 여부도 조회됩니다.
    - 내가 몇 번 발급 받았는지도 전달합니다.
    - ⚠️ 여러 번 받을 수 있는 쿠폰도 있어, 최대 받을 수 있는 쿠폰의 개수도 넣어뒀습니다.
    
    """)
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
                  "expireDate": "2025-07-11",
                  "quota": 30,
                  "remaining": 28,
                  "policy": "WEEKLY",
                  "couponName": "OUF 2025 사전 예약 주류 20% 할인 쿠폰",
                  "couponDescription": "뭔가를 할인받을 수 잇어요",
                  "howToUse": "이렇게  쓰시면 됩니다",
                  "notes": "이러면 안 돼요",
                  "soldOut": false
                }
              ]
            }
            """)))
    @ApiResponse(
            responseCode = "404",
            description = "베뉴가 존재하지 않음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "베뉴 없음", value = SwaggerExamples.VENUE_NOT_EXIST))
    )
    ResponseEntity<ResponseDTO<List<VenueCouponResponseDTO>>> getCouponsByVenue(@PathVariable Long venueId);
}
