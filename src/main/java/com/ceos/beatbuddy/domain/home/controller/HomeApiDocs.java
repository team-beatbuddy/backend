package com.ceos.beatbuddy.domain.home.controller;

import com.ceos.beatbuddy.domain.home.dto.KeywordResponseDTO;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;

public interface HomeApiDocs {
    @Operation(
            summary = "사용자가 고른 키워드를 나열합니다.",
            description = "사용자가 고른 mood, region, genre를 각각 응답합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "사용자가 고른 키워드 응답",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                                "status": 200,
                                "code": "SUCCESS_GET_MY_KEYWORD",
                                "message": "내가 선택한 키워드를 조회했습니다.",
                                "data": {
                                    "genres": [
                                        "SOUL&FUNK",
                                        "LATIN",
                                        "K-POP"
                                    ],
                                    "moods": [
                                        "EXOTIC"
                                    ],
                                    "regions": [
                                        "HONGDAE",
                                        "ITAEWON"
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
    )
    })
    ResponseEntity<ResponseDTO<KeywordResponseDTO>> getMyKeyword();

}
