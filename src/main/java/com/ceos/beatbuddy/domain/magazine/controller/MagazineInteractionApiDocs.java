package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface MagazineInteractionApiDocs {

    @Operation(summary = "매거진 좋아요\n",
            description = "매거진에 좋아요를 표시합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매거진에 좋아요를 표시합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_LIKE_MAGAZINE",
                              "message": "매거진에 성공적으로 좋아요를 표시했습니다.",
                              "data": "좋아요를 표시했습니다."
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 매거진", value = SwaggerExamples.NOT_FOUND_MAGAZINE)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 좋아요를 누른 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이미 좋아요를 누른 경우", value = SwaggerExamples.ALREADY_LIKED))
            )
    })
    ResponseEntity<ResponseDTO<String>> likeMagazine(@PathVariable Long magazineId);

    @Operation(
            summary = "매거진 좋아요 취소",
            description = "매거진에 좋아요를 취소합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "매거진에 좋아요를 취소합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                        {
                                          "status": 200,
                                          "code": "SUCCESS_DELETE_LIKE",
                                          "message": "좋아요를 취소했습니다.",
                                          "data": "좋아요를 취소했습니다"
                                        }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "리소스를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 매거진", value = SwaggerExamples.NOT_FOUND_MAGAZINE),
                                    @ExampleObject(name = "기존에 좋아요를 누르지 않았던 경우", value = SwaggerExamples.NOT_FOUND_LIKE)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> deleteLikeMagazine(@PathVariable Long magazineId);
}
