package com.ceos.beatbuddy.domain.home.controller;

import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.RecommendFilterDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface HomeApiDocs {
    @Operation(summary = "선탁한 선호 키워드 응답\n",
            description = "사용자의 선호도 키워드를 나열합니다.\n")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 선택한 키워드를 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                                  "status": 200,
                                  "code": "SUCCESS_GET_MY_KEYWORD",
                                  "message": "내가 선택한 키워드를 조회했습니다.",
                                  "data": [
                                        "SOUL&FUNK",
                                        "LATIN",
                                        "K-POP",
                                        "EXOTIC",
                                        "HONGDAE",
                                        "ITAEWON"
                                  ]
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<String>>> getMyKeyword();

    @Operation(
            summary = "사용자의 선호에 따른 베뉴 추천",
            description = "사용자의 선호도에 의해 추출된 추천 베뉴"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "나의 취향에 맞는 베뉴 5개를 불러왔습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                                "status": 200,
                                "code": "SUCCESS_GET_RECOMMEND_WITH_FAVORITE",
                                "message": "나의 취향에 맞는 베뉴 5개를 불러왔습니다.",
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
                                      "isHeartbeat": false,
                                      "isSmokingAllowed": true,
                                      "isFreeEntrance": false,
                                      "address": "서울특별시 마포구 서교동 358-1",
                                      "latitude": 37.5555,
                                      "longitude": 126.9255
                                    }
                                 ]
                            }
                        """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "요청한 유저가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)
                    )
            )
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
                                                  "isHeartbeat": false,
                                                  "isSmokingAllowed": true,
                                                  "isFreeEntrance": false,
                                                  "address": "서울특별시 마포구 서교동 358-1",
                                                  "latitude": 37.5555,
                                                  "longitude": 126.9255
                                                }
                                             ]
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "잘못된 요청 (존재하지 않는 지역/무드/장르/유저 등)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "존재하지 않는 지역",
                                            value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "REGION_NOT_EXIST",
                                  "message": "존재하지 않는 지역입니다."
                                }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 무드",
                                            value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MOOD_INDEX_NOT_EXIST",
                                  "message": "해당 문자열의 분위기는 리스트에 없습니다."
                                }
                            """
                                    ),
                                    @ExampleObject(
                                            name = "존재하지 않는 장르",
                                            value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "GENRE_INDEX_NOT_EXIST",
                                  "message": "해당 문자열의 장르는 리스트에 없습니다."
                                }
                            """
                                    ),
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
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "사전에 정해둔 리스트에 없는 무드/장르/지역명 전달 시 발생",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 400,
                              "error": "BAD_REQUEST",
                              "code": "INVALID_TAG",
                              "message": "태그 값이 올바르지 않습니다."
                            }
                        """)
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<VenueResponseDTO>>> recommendVenuesByFilter(@RequestBody RecommendFilterDTO recommendFilterDTO);

}
//
//@ApiResponse(
//        responseCode = "404",
//        description = "잘못된 요청 (존재하지 않는 지역/무드/장르/유저 등)",
//        content = @Content(
//                mediaType = "application/json",
//                examples = {
//                        @ExampleObject(
//                                name = "존재하지 않는 지역",
//                                value = """
//                        {
//                          "status": 404,
//                          "error": "NOT_FOUND",
//                          "code": "REGION_NOT_EXIST",
//                          "message": "존재하지 않는 지역입니다."
//                        }
//                    """
//                        ),
//                        @ExampleObject(
//                                name = "존재하지 않는 무드",
//                                value = """
//                        {
//                          "status": 404,
//                          "error": "NOT_FOUND",
//                          "code": "MOOD_INDEX_NOT_EXIST",
//                          "message": "해당 문자열의 분위기는 리스트에 없습니다."
//                        }
//                    """
//                        ),
//                        @ExampleObject(
//                                name = "존재하지 않는 장르",
//                                value = """
//                        {
//                          "status": 404,
//                          "error": "NOT_FOUND",
//                          "code": "GENRE_INDEX_NOT_EXIST",
//                          "message": "해당 문자열의 장르는 리스트에 없습니다."
//                        }
//                    """
//                        ),
//                        @ExampleObject(
//                                name = "존재하지 않는 유저",
//                                value = """
//                        {
//                          "status": 404,
//                          "error": "NOT_FOUND",
//                          "code": "MEMBER_NOT_EXIST",
//                          "message": "요청한 유저가 존재하지 않습니다."
//                        }
//                    """
//                        )
//                }
//        )
//),
//@ApiResponse(
//        responseCode = "400",
//        description = "사전에 정해둔 리스트에 없는 무드/장르/지역명 전달 시 발생",
//        content = @Content(
//                mediaType = "application/json",
//                schema = @Schema(implementation = ResponseDTO.class),
//                examples = @ExampleObject(value = """
//                {
//                  "status": 400,
//                  "error": "BAD_REQUEST",
//                  "code": "INVALID_TAG",
//                  "message": "태그 값이 올바르지 않습니다."
//                }
//            """)
//        )
//)
