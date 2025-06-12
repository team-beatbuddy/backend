package com.ceos.beatbuddy.domain.home.controller;

import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.RecommendFilterDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface RecommendApiDocs {
    @Operation(summary = "베뉴 추천\n",
            description = "사용자의 선호도에 의해 추출된 추천 베뉴를 조회합니다.\n"
                    + "현재는 5개의 베뉴를 추천하도록 설정했습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 베뉴를 조회하는데 성공했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject("""
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_RECOMMEND_WITH_FAVORITE",
                              "message": "나의 취향에 맞는 베뉴 5개를 불러왔습니다.",
                              "data": [
                                {
                                  "venueId": 154,
                                  "englishName": "ZENBAR",
                                  "koreanName": "젠바",
                                  "tagList": ["HIPHOP", "LATIN"],
                                  "heartbeatNum": 1,
                                  "logoUrl": "https://example.com/logo.jpg",
                                  "backgroundUrl": ["https://example.com/bg.mp4"],
                                  "isHeartbeat": false
                                }
                              ]
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 지역 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "status": 404,
                              "error": "NOT_FOUND",
                              "code": "REGION_NOT_EXIST",
                              "message": "존재하지 않는 지역입니다."
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 무드 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "status": 404,
                              "error": "NOT_FOUND",
                              "code": "MOOD_INDEX_NOT_EXIST",
                              "message": "해당 문자열의 분위기는 리스트에 없습니다."
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 장르 입력",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = """
                            {
                              "status": 404,
                              "error": "NOT_FOUND",
                              "code": "GENRE_INDEX_NOT_EXIST",
                              "message": "해당 문자열의 장르는 리스트에 없습니다."
                            }
                        """)
                    )
            ),


    })
    ResponseEntity<ResponseDTO<List<VenueResponseDTO>>> recommendVenues();


    @Operation(summary = "베뉴 추천 결과에서 필터링 작업\n",
            description = "사용자의 선호도에 의해 추출된 추천 베뉴에서 사용자가 선택한 요소에 의해 필터링한 결과를 반환합니다.\n"
                    + "현재는 5개의 베뉴를 추천하도록 설정했습니다.(추후 변경 가능)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "추천 베뉴를 조회하는데 성공했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                        {
                                            "status": 200,
                                            "code": "SUCCESS_GET_RECOMMEND_WITH_FAVORITE_AND_FILTER",
                                            "message": "선호도와 필터 조건에 맞는 베뉴 5개를 불러왔습니다.",
                                            "data": [
                                                {
                                                  "venueId": 154,
                                                  "englishName": "ZENBAR",
                                                  "koreanName": "젠바",
                                                  "tagList": [
                                                    "HIPHOP",
                                                    "LATIN"
                                                  ],
                                                  "heartbeatNum": 1,
                                                  "logoUrl": "https://example.com/logo.jpg",
                                                  "backgroundUrl": [
                                                    "https://example.com/bg.mp4"
                                                  ],
                                                  "isHeartbeat": false
                                                }
                                             ]
                                        }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다 or 유저의 취향이 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class))),
            @ApiResponse(responseCode = "400", description = "사전에 정해둔 무드&장르 리스트, 지역명 리스트에 없는 태그를 전달하면 400 에러가 뜹니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    ResponseEntity<List<VenueResponseDTO>> recommendVenuesByFilter(@RequestBody RecommendFilterDTO recommendFilterDTO);
}
