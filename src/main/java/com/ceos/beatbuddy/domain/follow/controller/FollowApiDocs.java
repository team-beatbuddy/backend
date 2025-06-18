package com.ceos.beatbuddy.domain.follow.controller;

import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

public interface FollowApiDocs {
    @Operation(summary ="팔로우 기능\n",
            description = "다른 사람을 팔로우할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "성공적으로 팔로우했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_FOLLOW",
                              "message": "성공적으로 팔로우했습니다.",
                              "data": {
                                "followerId": 156,
                                "followingId": 140
                              }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저 또는 내가 팔로우하려는 유저가 없는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    {@ExampleObject(
                                            name = "존재하지 않는 유저",
                                            value = """
                                                        {
                                                          "status": 404,
                                                          "error": "NOT_FOUND",
                                                          "code": "MEMBER_NOT_EXIST",
                                                          "message": "요청한 유저가 존재하지 않습니다."
                                                        }
                                                    """
                                    ),
                                            @ExampleObject(
                                                    name = "팔로우 대상이 존재하지 않는 경우",
                                                    value = """
                                                                {
                                                                  "status": 404,
                                                                  "error": "NOT_FOUND",
                                                                  "code": "FOLLOWING_TARGET_NOT_FOUND",
                                                                  "message": "팔로우 대상이 존재하지 않습니다."
                                                                }
                                                            """
                                            )}

                    )
            ),
            @ApiResponse(
                    responseCode = "406",
                    description = "자기 자신을 팔로우 하려는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "자기 자신을 팔로우 하려는 경우",
                                            value = """
                                                        {
                                                          "status": 406,
                                                          "error": "NOT_ACCEPTABLE",
                                                          "code": "CANNOT_FOLLOW_SELF",
                                                          "message": "자기 자신은 팔로우할 수 없습니다."
                                                        }
                                                    """
                                    )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 팔로우한 대상인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(
                                    name = "이미 팔로우한 경우",
                                    value = """
                                                        {
                                                          "status": 409,
                                                          "error": "CONFLICT",
                                                          "code": "ALREADY_FOLLOWED",
                                                          "message": "이미 팔로우한 대상입니다."
                                                        }
                                                    """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<FollowResponseDTO>> addFollow(@PathVariable Long followingId);
}
