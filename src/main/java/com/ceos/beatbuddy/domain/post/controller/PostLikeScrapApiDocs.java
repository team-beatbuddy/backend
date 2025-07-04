package com.ceos.beatbuddy.domain.post.controller;

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

public interface PostLikeScrapApiDocs {
    @Operation(summary = "포스트 좋아요\n",
            description = "포스트 좋아요 눌기")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "좋아요를 눌렀습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS_LIKE_POST",
                                      "message": "좋아요를 눌렀습니다.",
                                      "data": "좋아요를 눌렀습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저, 존재하지 않는 포스트인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    {
                                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST ),
                                            @ExampleObject(name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)

                                    }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 좋아요한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "이미 좋아요한 경우", value = SwaggerExamples.ALREADY_LIKED)))
    })
    ResponseEntity<ResponseDTO<String>> addPostLike(@PathVariable Long postId);

    @Operation(summary = "포스트 좋아요 취소\n",
            description = "포스트에 눌렀던 좋아요를 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "좋아요가 취소되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                {
                                  "status": 200,
                                  "code": "SUCCESS_CANCEL_LIKE_POST",
                                  "message": "좋아요를 취소했습니다.",
                                  "data": "좋아요를 취소했습니다."
                                }
                                """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "좋아요를 누른 적 없는 경우, 유저가 존재하지 않는 경우, 글이 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name="삭제할 좋아요가 존재하지 않음.", value = SwaggerExamples.NOT_FOUND_LIKE),
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> deletePostLike(@PathVariable Long postId);



    @Operation(summary = "포스트 스크랩\n",
            description = "포스트를 스크랩합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "포스트를 스크랩했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 201,
                                      "code": "SUCCESS_SCRAP_POST",
                                      "message": "스크랩을 완료했습니다.",
                                      "data": "스크랩을 완료했습니다."
                                    }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저, 존재하지 않는 포스트인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    {
                                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                            @ExampleObject(name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)

                                    }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 스크랩한 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "이미 스크랩한 경우", value = SwaggerExamples.ALREADY_SCRAPPED )
                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> scrapPost(@PathVariable Long postId);

    @Operation(summary = "포스트 스크랩 취소\n",
            description = "포스트에 눌렀던 스크랩을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩이 취소되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_DELETE_SCRAP",
                              "message": "스크랩을 취소했습니다.",
                              "data": "스크랩을 취소했습니다."
                            }
                                """)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "스크랩을 누른 적 없는 경우, 유저가 존재하지 않는 경우, 글이 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name="삭제할 스크랩이 존재하지 않음.", value = SwaggerExamples.NOT_FOUND_SCRAP),
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)
                            }
                    )
            )
    })

    ResponseEntity<ResponseDTO<String>> deleteScrapPost(@PathVariable Long postId);
}
