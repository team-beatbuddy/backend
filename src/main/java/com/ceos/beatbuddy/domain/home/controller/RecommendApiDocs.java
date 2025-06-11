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
                            examples = @ExampleObject(value = """
                            {
                                "status": 200,
                                "code": "SUCCESS_GET_MY_KEYWORD",
                                "message": "내가 선택한 키워드를 조회했습니다.",
                                "data": {
                                    [
                                                  {
                                                    "venueId": 88,
                                                    "englishName": "Casa Corona Seoul",
                                                    "koreanName": "카사 코로나 서울",
                                                    "tagList": [
                                                      "HOUSE",
                                                      "SOUL&FUNK",
                                                      "ROOFTOP",
                                                      "CHILL",
                                                      "EXOTIC",
                                                      "ITAEWON"
                                                    ],
                                                    "heartbeatNum": 3,
                                                    "logoUrl": "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/bfa2939f-6%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EB%A1%9C%EA%B3%A0.jpg",
                                                    "backgroundUrl": [
                                                      "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/052ebd9e-d%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EC%82%AC%EC%A7%841.jpg",
                                                      "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/54a970f5-d%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EC%82%AC%EC%A7%842.jpg"
                                                    ],
                                                    "isHeartbeat": false
                                                  } 
                                                  ...
                                                ]
                                }
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
                                            "code": "SUCCESS_GET_MY_KEYWORD",
                                            "message": "내가 선택한 키워드를 조회했습니다.",
                                            "data": {
                                                [
                                                              {
                                                                "venueId": 88,
                                                                "englishName": "Casa Corona Seoul",
                                                                "koreanName": "카사 코로나 서울",
                                                                "tagList": [
                                                                  "HOUSE",
                                                                  "SOUL&FUNK",
                                                                  "ROOFTOP",
                                                                  "CHILL",
                                                                  "EXOTIC",
                                                                  "ITAEWON"
                                                                ],
                                                                "heartbeatNum": 3,
                                                                "logoUrl": "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/bfa2939f-6%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EB%A1%9C%EA%B3%A0.jpg",
                                                                "backgroundUrl": [
                                                                  "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/052ebd9e-d%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EC%82%AC%EC%A7%841.jpg",
                                                                  "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/54a970f5-d%EC%B9%B4%EC%82%AC%20%EC%BD%94%EB%A1%9C%EB%82%98%20%EC%84%9C%EC%9A%B8_%EC%82%AC%EC%A7%842.jpg"
                                                                ],
                                                                "isHeartbeat": false
                                                              } 
                                                              ...
                                                            ]
                                            }
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
