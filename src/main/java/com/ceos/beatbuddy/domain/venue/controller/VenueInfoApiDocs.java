package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueCouponResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

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
                  "maxQuota": 2,
                  "receivedCount": 2,
                  "received": true,
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



    @Operation(summary = "베뉴의 이벤트 목록 조회",
            description = """
                - isPast 파라미터가 true인 경우, 과거 이벤트를 조회합니다.
                - isPast 파라미터가 false인 경우, 현재 및 예정 이벤트를 조회합니다.
                - ⚠️ 주의) 현재 및 예정 이벤트를 모두 조회한 뒤에는 Past를 true로 받아 과거 이벤트를 조회해야 합니다.
    """)
    @ApiResponse(
            responseCode = "200",
            description = "이벤트 목록 조회 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "조회 성공", value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_VENUE_EVENTS",
                      "message": "베뉴의 이벤트 목록을 성공적으로 조회했습니다.",
                      "data": {
                        "sort": "latest",
                        "page": 1,
                        "size": 10,
                        "totalSize": 7,
                        "eventResponseDTOS": [
                          {
                            "eventId": 5,
                            "title": "이벤트 제목",
                            "content": "내용입니다.",
                            "thumbImage": "",
                            "liked": true,
                            "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                            "likes": 1,
                            "views": 0,
                            "startDate": "2025-04-20T00:00:00",
                            "endDate": "2025-07-21T00:00:00",
                            "region": "강남_신사",
                            "isFreeEntrance": false,
                            "isAttending": false,
                            "isAuthor": true
                          },
                          {
                            "eventId": 7,
                            "title": "이벤트 제목",
                            "content": "내용입니다.",
                            "thumbImage": "",
                            "liked": false,
                            "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                            "likes": 0,
                            "views": 20,
                            "startDate": "2025-06-20T00:00:00",
                            "endDate": "2025-07-22T00:00:00",
                            "region": "이태원",
                            "isFreeEntrance": false,
                            "isAttending": false,
                            "isAuthor": true
                          }
                        ]
                      }
                    }
            """))
    )
    @ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = {@ExampleObject(name = "베뉴 없음", value = SwaggerExamples.VENUE_NOT_EXIST),
                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)})
    )
    @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS))
    )
    ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventsByVenueNowAndUpcomingLatest(@PathVariable Long venueId,
                                                                                           @RequestParam(defaultValue = "1") int page,
                                                                                           @RequestParam(defaultValue = "10") int size,
                                                                                           @RequestParam(defaultValue = "false") boolean isPast);

}
