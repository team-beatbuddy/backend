package com.ceos.beatbuddy.domain.event.controller;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

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
                      "data": [
                        {
                          "eventId": 2,
                          "title": "string",
                          "content": "string",
                          "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                          "liked": true,
                          "location": "string",
                          "likes": 2,
                          "views": 12,
                          "startDate": "2025-06-18T00:00:00",
                          "endDate": "2025-06-23T00:00:00",
                          "region": "홍대",
                          "isFreeEntrance": false,
                          "isAttending": true,
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
                          "startDate": "2025-08-22T00:00:00",
                          "endDate": "2025-09-21T00:00:00",
                          "region": "홍대",
                          "isFreeEntrance": false,
                          "isAttending": true,
                          "isAuthor": true
                        },
                        {
                          "eventId": 14,
                          "title": "이벤트 제목",
                          "content": "내용입니다.",
                          "thumbImage": "",
                          "liked": false,
                          "location": "서울시 강남구 테헤란로 123, venue 를 고르셨다면 해당 장소의 주소를 넣어주세요.",
                          "likes": 0,
                          "views": 0,
                          "startDate": "2025-08-22T00:00:00",
                          "endDate": "2025-09-21T00:00:00",
                          "region": "홍대",
                          "isFreeEntrance": false,
                          "isAttending": false,
                          "isAuthor": true
                        }
                      ]
                    }
                            """)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "리소스 없음",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = SwaggerExamples.MEMBER_NOT_EXIST)
            )
    )
    @ApiResponse(
            responseCode = "500",
            description = "검색 실패",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(value = SwaggerExamples.ELASTICSEARCH_SEARCH_FAILED)
            )
    )
    ResponseEntity<ResponseDTO<List<EventResponseDTO>>> searchEvents(
            @RequestParam String keyword
    );
}
