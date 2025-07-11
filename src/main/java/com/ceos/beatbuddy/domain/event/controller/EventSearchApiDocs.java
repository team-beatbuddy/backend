package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventSearchListResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

public interface EventSearchApiDocs {
    @Operation(summary = "이벤트 검색", description = """
            키워드를 통해 검색할 수 있습니다.
            - admin은 visible 이 False 인 것까지 확인 가능합니다.
            - 일반 유저는 visible 이 True 인 것만 확인 가능합니다.
            - 유료입장/무료입장 도 검색 가능합니다.
            - upcoming/now/past 의 카테고리로 분리되어 한번에 불러와집니다.
            """)
    @ApiResponse(
            responseCode = "200",
            description = "검색 성공",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "EVENT_SEARCH_SUCCESS",
                              "message": "이벤트 검색을 성공적으로 했습니다.",
                              "data": {
                                "eventResponseDTOS": {
                                  "past": [
                                    {
                                      "eventId": 1,
                                      "title": "이벤트 시작",
                                      "content": "이게 바로 이트",
                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                                      "liked": false,
                                      "location": "경기도 파주",
                                      "likes": 5,
                                      "views": 29,
                                      "startDate": "2025-06-17",
                                      "endDate": "2025-06-17",
                                      "region": "강남_신사",
                                      "isFreeEntrance": false,
                                      "isAttending": true,
                                      "isAuthor": true
                                    },
                                    {
                                      "eventId": 2,
                                      "title": "string",
                                      "content": "string",
                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                      "liked": true,
                                      "location": "string",
                                      "likes": 2,
                                      "views": 12,
                                      "startDate": "2025-06-18",
                                      "endDate": "2025-06-23",
                                      "region": "홍대",
                                      "isFreeEntrance": false,
                                      "isAttending": true,
                                      "isAuthor": true
                                    }
                                  ],
                                  "now": [
                                    {
                                      "eventId": 4,
                                      "title": "이벤트 제목",
                                      "content": "내용입니다.",
                                      "thumbImage": "",
                                      "liked": true,
                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                      "likes": 2,
                                      "views": 354,
                                      "startDate": "2025-06-24",
                                      "endDate": "2025-07-21",
                                      "region": "이태원",
                                      "isFreeEntrance": false,
                                      "isAttending": true,
                                      "isAuthor": true
                                    },
                                    {
                                      "eventId": 5,
                                      "title": "이벤트 제목",
                                      "content": "내용입니다.",
                                      "thumbImage": "",
                                      "liked": true,
                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                      "likes": 1,
                                      "views": 0,
                                      "startDate": "2025-04-20",
                                      "endDate": "2025-07-21",
                                      "region": "강남_신사",
                                      "isFreeEntrance": false,
                                      "isAttending": false,
                                      "isAuthor": true
                                    }
                                  ],
                                  "upcoming": [
                                    {
                                      "eventId": 3,
                                      "title": "이벤트 제목",
                                      "content": "내용입니다.",
                                      "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/event/20250622_175153_c7950d03-c0d4-4d6f-a9c5-94896f3b5185.png",
                                      "liked": false,
                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                      "likes": 2,
                                      "views": 5,
                                      "startDate": "2025-08-22",
                                      "endDate": "2025-09-21",
                                      "region": "강남_신사",
                                      "dday": "D-42",
                                      "isFreeEntrance": false,
                                      "isAttending": false,
                                      "isAuthor": true
                                    },
                                    {
                                      "eventId": 12,
                                      "title": "이벤트 제목",
                                      "content": "내용입니다.",
                                      "thumbImage": "",
                                      "liked": false,
                                      "location": "아직 정해지지 않음... 여기 elastic search 쓸 것 같음",
                                      "likes": 1,
                                      "views": 6,
                                      "startDate": "2025-08-22",
                                      "endDate": "2025-09-21",
                                      "region": "홍대",
                                      "dday": "D-42",
                                      "isFreeEntrance": false,
                                      "isAttending": true,
                                      "isAuthor": true
                                    }
                                  ]
                                }
                              }
                            }
                            """)
            )
    )
    ResponseEntity<ResponseDTO<EventSearchListResponseDTO>> searchEvents(
            @RequestParam String keyword
    ) throws IOException;
}
