package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.dto.CouponReceiveResponseDTO;
import com.ceos.beatbuddy.domain.coupon.dto.MyPageCouponList;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

public interface CouponApiDocs {
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


    @Operation(summary = "내 쿠폰 조회", description = "내 쿠폰 목록을 조회합니다.")
    @ApiResponse(
            responseCode = "200",
            description = "내 쿠폰 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "내 쿠폰 조회 성공 예시",
                                    value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_MEMBER_COUPON_LIST",
                                      "message": "내 쿠폰 목록을 성공적으로 조회했습니다.",
                                      "data": {
                                        "totalCount": 3,
                                        "totalPage": 1,
                                        "currentPage": 1,
                                        "pageSize": 10,
                                        "coupons": [
                                          {
                                            "memberCouponId": 1,
                                            "couponId": 1,
                                            "memberId": 156,
                                            "couponName": "1번 테스트",
                                            "couponContent": "이건 이렇게 이렇게 할인해주는 거고여",
                                            "howToUse": "우리한테 보여주세여",
                                            "venueName": "클럽 트립",
                                            "receivedDate": "2025-07-08T23:43:08.28746",
                                            "usedDate": null,
                                            "expirationDate": "2025-07-13",
                                            "status": "RECEIVED"
                                          },
                                          {
                                            "memberCouponId": 3,
                                            "couponId": 2,
                                            "memberId": 156,
                                            "couponName": "2번 테스트",
                                            "couponContent": "이거는 이렇게 이렇게 할인해주는 거고여 예예예",
                                            "howToUse": "거기 직원에 보여주세요",
                                            "venueName": "클럽 트립",
                                            "receivedDate": "2025-07-08T23:43:08.28746",
                                            "usedDate": null,
                                            "expirationDate": "2025-07-13",
                                            "status": "RECEIVED"
                                          },
                                          {
                                            "memberCouponId": 4,
                                            "couponId": 2,
                                            "memberId": 156,
                                            "couponName": "2번 테스트",
                                            "couponContent": "이거는 이렇게 이렇게 할인해주는 거고여 예예예",
                                            "howToUse": "거기 직원에 보여주세요",
                                            "venueName": "클럽 트립",
                                            "receivedDate": "2025-07-08T23:43:08.28746",
                                            "usedDate": null,
                                            "expirationDate": "2025-07-13",
                                            "status": "RECEIVED"
                                          }
                                        ]
                                      }
                                    }
                                            """,
                                    description = "내 쿠폰 조회 성공 시 응답 예시"
                            ),
                            @ExampleObject(name = "쿠폰이 없을 때", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST, description = "쿠폰이 없을 때의 응답 예시")
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS, description = "잘못된 페이지 요청 시의 응답 예시")
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "리스소 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
            )
    )
    ResponseEntity<ResponseDTO<MyPageCouponList>> getMyAvailableCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);


    @Operation(summary = "내 사용 불가능 쿠폰 조회", description = """
    내 사용 불가능 쿠폰 목록을 조회합니다.
    - status 가 USED 이면  사용을 한 쿠폰입니다.
    - expirationDate 가 오늘 이전이면 만료된 쿠폰입니다.
    """)
    @ApiResponse(
            responseCode = "200",
            description = "내 사용 불가능 쿠폰 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(
                                    name = "내 사용 불가능 쿠폰 조회 성공 예시",
                                    value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_MEMBER_COUPON_LIST",
                                      "message": "내 쿠폰 목록을 성공적으로 조회했습니다.",
                                      "data": {
                                        "totalCount": 1,
                                        "totalPage": 1,
                                        "currentPage": 1,
                                        "pageSize": 10,
                                        "coupons": [
                                          {
                                            "memberCouponId": 3,
                                            "couponId": 2,
                                            "memberId": 156,
                                            "couponName": "1번 테스트",
                                            "couponContent": null,
                                            "howToUse": "string",
                                            "venueName": "클럽 트립",
                                            "receivedDate": "2025-07-08T23:43:08.28746",
                                            "usedDate": "2025-07-10T15:08:34.864157",
                                            "expirationDate": "2025-07-13",
                                            "status": "USED"
                                          }
                                        ]
                                      }
                                    }
                                            """,
                                    description = "내 사용 불가능 쿠폰 조회 성공 시 응답 예시"
                            ),
                            @ExampleObject(name = "쿠폰이 없을 때", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST, description = "쿠폰이 없을 때의 응답 예시")
                    }
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS, description = "잘못된 페이지 요청 시의 응답 예시")
                    }
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
            )
    )
    ResponseEntity<ResponseDTO<MyPageCouponList>> getMyUnavailableCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);
}
