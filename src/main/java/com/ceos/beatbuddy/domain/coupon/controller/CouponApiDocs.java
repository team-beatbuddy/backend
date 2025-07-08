package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.dto.CouponReceiveResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface CouponApiDocs {

    /**
     * 쿠폰을 수령하는 API 문서화
     *
     * @param couponId 쿠폰 ID
     * @return ResponseEntity<ResponseDTO<CouponReceiveResponseDTO>> 쿠폰 수령 응답
     */
    @Operation(summary = "쿠폰 수령", description = "쿠폰을 수령합니다.")
    @ApiResponse(
            responseCode = "201",
            description = "쿠폰 발급 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "쿠폰 수령 성공 예시",
                                    value = """
                                            {
                                              "status": 201,
                                              "code": "SUCCESS_RECEIVE_COUPON",
                                              "message": "쿠폰을 성공적으로 발급했습니다.",
                                              "data": {
                                                "couponId": 2,
                                                "receivedDate": "2025-07-07",
                                                "expireDate": "2025-07-07"
                                              }
                                            }
                                            """,
                                    description = "쿠폰 수령 성공 시 응답 예시"
                            )
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "쿠폰 수령 실패",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 남은 수량이 없을 때", value = SwaggerExamples.COUPON_QUOTA_SOLD_OUT, description = "쿠폰 수령 실패 시 응답 예시"),
                            @ExampleObject(name = "쿠폰 수량 초기화 실패인데 수령하고자 했을 때", value = SwaggerExamples.COUPON_QUOTA_NOT_INITIALIZED, description = "쿠폰 수량이 초기화되지 않았을 때의 응답 예시"),
                            @ExampleObject(name = "만료된 쿠폰 수령 시", value = SwaggerExamples.COUPON_EXPIRED, description = "쿠폰이 만료되었을 때의 응답 예시"),
                            @ExampleObject(name = "쿠폰 수령 실패", value = SwaggerExamples.COUPON_DISABLED, description = "활성화되지 않은 쿠폰을 수령하려 할 때의 응답 예시"),
                            @ExampleObject(name = "쿠폰 수령 한도 초과", value = SwaggerExamples.COUPON_RECEIVE_LIMIT_EXCEEDED, description = "쿠폰 수령 한도를 초과했을 때의 응답 예시"),
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "쿠폰을 찾을 수 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 수령 실패 예시", value = SwaggerExamples.COUPON_NOT_FOUND, description = "존재하지 않는 쿠폰을 수령하려 할 때의 응답 예시")
                    }
            )
    )
    @ApiResponse(
            responseCode = "409",
            description = "쿠폰 수령 충돌",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "한번만 받는 쿠폰, 추가 발급 요청 시", value = SwaggerExamples.COUPON_ALREADY_RECEIVED, description = "이미 받은 쿠폰을 다시 수령하려 할 때의 응답 예시"),
                            @ExampleObject(name = "오늘 쿠폰 받고, 추가 발급 요청 시", value = SwaggerExamples.COUPON_ALREADY_RECEIVED_TODAY, description = "오늘 이미 받은 쿠폰을 다시 수령하려 할 때의 응답 예시")
                    }
            )
    )
    /**
     * 쿠폰을 수령하는 API 문서화
     *
     * @param venueId 장소 ID
     * @param couponId 쿠폰 ID
     * @return ResponseEntity<ResponseDTO<CouponReceiveResponseDTO>> 쿠폰 수령 응답
     */
    ResponseEntity<ResponseDTO<CouponReceiveResponseDTO>> receiveCoupon(
            @PathVariable Long venueId,
            @PathVariable Long couponId);
}
