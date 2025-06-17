package com.ceos.beatbuddy.domain.mypage.controller;

import com.ceos.beatbuddy.domain.event.dto.EventResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface MyPageApiDocs {

    @Operation(
            summary = "마이페이지 이벤트 조회\n",
            description = "마이페이지에서 이벤트를 조회합니다. 비즈니스 회원인 경우 본인이 작성한 글, 일반 회원의 경우 좋아요 누르거나 참여한 글"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "마이페이지 이벤트 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "마이페이지 이벤트 조회 성공",
                                            value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_GET_MY_EVENTS",
                                          "message": "마이페이지의 이벤트를 성공적으로 조회했습니다",
                                          "data": {
                                            "past": [
                                              {
                                                "eventId": 1,
                                                "title": "이벤트 시작",
                                                "content": "이게 바로 이트",
                                                "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ddded007-dGroup%201000003259.png",
                                                "location": "경기도 파주",
                                                "likes": 5,
                                                "views": 0,
                                                "startDate": "2025-06-17",
                                                "endDate": "2025-06-17"
                                              }
                                            ],
                                            "upcoming": [
                                              {
                                                "eventId": 2,
                                                "title": "string",
                                                "content": "string",
                                                "image": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ae7cd814-fGroup%201000003259.png",
                                                "location": "string",
                                                "likes": 2,
                                                "views": 0,
                                                "startDate": "2025-06-18",
                                                "endDate": "2025-06-23",
                                                "dday": "D-0"
                                              }
                                            ]
                                          }
                                        }
                    """
                                    ),
                                    @ExampleObject(
                                            name = "빈 이벤트 리스트",
                                            value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_BUT_EMPTY_LIST",
                                          "message": "성공적으로 조회했으나 리스트가 비었습니다.",
                                          "data": []
                                        }
                    """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "유저 정보가 없습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 유저",
                                            value = """
                    {
                      "status": 404,
                      "error": "NOT_FOUND",
                      "code": "MEMBER_NOT_EXIST",
                      "message": "요청한 유저가 존재하지 않습니다."
                    }
                    """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<Map<String, List<EventResponseDTO>>>> getMyEvents();
}
