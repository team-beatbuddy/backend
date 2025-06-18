package com.ceos.beatbuddy.domain.follow.controller;

import com.ceos.beatbuddy.domain.follow.dto.FollowResponseDTO;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

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





    @Operation(summary ="팔로우 취소 기능\n",
            description = "다른 사람을 팔로우 했던 것을 취소할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "성공적으로 팔로우를 취소했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_FOLLOW_DELETE",
                              "message": "성공적으로 팔로우를 취소했습니다.",
                              "data": "팔로우 취소 완료"
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "팔로우를 한 적이 없던 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(
                                            name = "팔로우를 한 적이 없던 경우",
                                            value = """
                                                        {
                                                          "status": 404,
                                                          "error": "NOT_FOUND",
                                                          "code": "FOLLOW_NOT_FOUND",
                                                          "message": "팔로우 관계가 존재하지 않습니다."
                                                        }
                                                    """
                                    )

                    )
            )
    })
    ResponseEntity<ResponseDTO<String>> deleteFollow(@Valid @PathVariable Long followingId);

    @Operation(summary ="팔로잉 목록 조회 기능\n",
            description = "내가 팔로잉하는 사람들을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 팔로잉하는 사람들 조회, 조회했으나 리스트가 비어있는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {@ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_FOLLOWINGS",
                                      "message": "내가 팔로우하는 목록을 가져왔습니다.",
                                      "data": [
                                        {
                                          "followerId": 156,
                                          "followingId": 140
                                        }
                                      ]
                                    }
                                    """),
                                    @ExampleObject(
                                            name = "팔로잉 한 사람이 없는 경우",
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
                    description = "존재하지 않는 유저인 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
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

                    )
            )
    })
    ResponseEntity<ResponseDTO<List<FollowResponseDTO>>> getFollowings();
}
