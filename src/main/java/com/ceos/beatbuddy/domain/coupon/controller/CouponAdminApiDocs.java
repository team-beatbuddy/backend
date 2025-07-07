package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.dto.CouponCreateRequestDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

public interface CouponAdminApiDocs {

    @Operation(summary = "쿠폰 등록", description = """
        새로운 쿠폰을 등록합니다.
        
        - 하나의 쿠폰은 여러 개의 업장(venueIds)에 동시에 적용될 수 있습니다.
        - couponPolicy는 DAILY(매일 발급), ONCE(한 번만 발급), WEEKLY(주 1회 발급) 중 하나입니다.
        - DAILY: 당일에 발급받은 경우 다시 받을 수 없습니다. 다음 날부터 재발급 가능합니다.
        - ONCE: 평생에 한 번만 발급 가능하며, 다시 발급받을 수 없습니다.
        - WEEKLY: 매주 n회만 발급 가능하며, 같은 주 내에 다시 받을 수 없습니다.
        
        - 쿠폰 등록 시 수량(quota)이 반드시 초기화되어야 하며, 그렇지 않으면 발급이 불가능합니다.
        - 유효기간(expireDate)이 지난 날짜인 경우 쿠폰 등록이 불가능합니다.
        - 업장 ID 리스트(venueIds)는 필수이며, 존재하지 않는 업장이 포함되면 등록이 실패합니다.
        
        - maxReceiveCountPerUser: 한 명당 최대로 받을 수 있는 쿠폰 수입니다. 정책이 기준입니다. (couponPolicy)
        - sameVenueUse: 같은 장소에서 사용할 수 있는 최대 횟수입니다. 정책이 기준입니다. (couponPolicy)
        
        정책 예시:
        - CouponPolicy.WEEKLY, maxReceiveCountPerUser = 2, sameVenueUse = 1
            → 유저는 한 주 동안 쿠폰을 최대 2개 받을 수 있고, 같은 업장에서 그 중 1개만 사용할 수 있습니다.

        - CouponPolicy.DAILY, maxReceiveCountPerUser = 1, sameVenueUse = 1
            → 유저는 하루에 1개만 받을 수 있고, 받은 쿠폰은 해당 업장에서 1회만 사용할 수 있습니다.
            
        - 만약 업장이 여러 개에서 진행되고, 10개의 쿠폰을 받을 수 있고 일주일에 같은 업장에서는 1개만 사용할 수 있다.
          CouponPolicy.WEEKLY, maxReceiveCountPerUser = 10, sameVenueUse = 1, VenueIds = [1,2,3,4,5,6,7,8,9,10]으로 적어주시면 됩니다.
            → 유저는 일주일 동안 10개의 쿠폰을 받을 수 있고, 같은 업장에서는 1개만 사용할 수 있습니다.
        """)
    @ApiResponse(responseCode = "200", description = "쿠폰 등록 성공",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(name = "쿠폰 등록 성공 예시",
                            value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS_CREATE_COUPON",
                                      "message": "쿠폰을 성공적으로 등록했습니다.",
                                      "data": "쿠폰 등록 성공"
                                    }
                                    """)))
    @ApiResponse(responseCode = "400", description = "쿠폰 등록 실패",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 수량 초기화 X", value = SwaggerExamples.COUPON_QUOTA_NOT_INITIALIZED, description = "쿠폰 수량이 초기화되지 않았을 때의 응답 예시"),
                            @ExampleObject(name = "쿠폰을 만료된 날짜로 등록 시", value = SwaggerExamples.COUPON_EXPIRED, description = "쿠폰이 만료되었을 때의 응답 예시"),
                            @ExampleObject(name = "잘못된 쿠폰 정책", value = SwaggerExamples.COUPON_INVALID_POLICY, description = "쿠폰 정책이 잘못되었을 때의 응답 예시"),
                    }))
    @ApiResponse(responseCode = "404", description = "리소스 없음",
            content = @Content(mediaType = "application/json",
                    examples = {@ExampleObject(name = "존재하지 않는 베뉴", value = SwaggerExamples.VENUE_NOT_EXIST, description = "존재하지 않는 업장에 쿠폰을 등록하려 할 때의 응답 예시"),
                            @ExampleObject(name = "존재하지 않는 멤버", value = SwaggerExamples.MEMBER_NOT_EXIST, description = "존재하지 않는 멤버에 쿠폰을 등록하려 할 때의 응답 예시")}))
    ResponseEntity<ResponseDTO<String>> createCoupon(@RequestBody CouponCreateRequestDTO request);


    @Operation(summary = "쿠폰 사용", description = "쿠폰을 사용합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "쿠폰 사용 성공",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "쿠폰 사용 성공 예시",
                                    value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_USE_COUPON",
                                              "message": "쿠폰을 성공적으로 사용했습니다.",
                                              "data": "쿠폰 사용 성공"
                                            }
                                            """))),
            @ApiResponse(responseCode = "404", description = "쿠폰을 찾을 수 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(name = "존재하지 않는 쿠폰", value = SwaggerExamples.NOT_FOUND_COUPON_RECEIVE, description = "존재하지 않는 쿠폰을 사용하려 할 때의 응답 예시"),
                                    @ExampleObject(name = "admin이 등록하지 않은 쿠폰", value = SwaggerExamples.COUPON_NOT_FOUND, description = "존재하지 않는 쿠폰을 사용하려 할 때의 응답 예시"),
                                    @ExampleObject(name = "존재하지 않는 멤버", value = SwaggerExamples.MEMBER_NOT_EXIST, description = "존재하지 않는 멤버에 쿠폰을 사용하려 할 때의 응답 예시")}))
    })
    @ApiResponse(responseCode = "400", description = "쿠폰 사용 실패",
            content = @Content(mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "쿠폰 만료", value = SwaggerExamples.COUPON_EXPIRED, description = "쿠폰이 만료되었을 때의 응답 예시"),
                            @ExampleObject(name = "이미 사용된 쿠폰", value = SwaggerExamples.COUPON_ALREADY_USED, description = "이미 사용된 쿠폰을 다시 사용하려 할 때의 응답 예시")
                    }))
    ResponseEntity<ResponseDTO<String>> useCoupon(
            @PathVariable Long receiveCouponId);
}
