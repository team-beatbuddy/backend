package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.RecommendFilterDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MagazineApiDocs {
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
                                            
                                            }
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
    ResponseEntity<ResponseDTO<?>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);

}
