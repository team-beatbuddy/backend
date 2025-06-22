package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.global.SwaggerExamples;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PostApiDocs {
    @Operation(summary = "#####게시물 생성 - 새로운 버전", description = "게시물을 생성합니다 (type: free/piece), 공통으로는 title(필수), content(필수), anonymous, venueId 입니다." +
            "free: hashtag, piece: totalPrice, totalMembers, eventDate; ")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게시물 생성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATE_POST",
                              "message": "포스트를 작성했습니다.",
                              "data": {
                                "id": 21,
                                "title": "string",
                                "role": "BUSINESS",
                                "likes": 0,
                                "comments": 0,
                                "createAt": "2025-06-19",
                                "nickname": "길동hong"
                              }
                            }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 게시글 타입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저, 베뉴가 존재하지 않는 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 베뉴", value = SwaggerExamples.VENUE_NOT_EXIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3에 이미지 등록 실패했을 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<ResponsePostDto>> addNewPost(
            @PathVariable String type,
            @Valid
            @RequestPart("postCreateRequestDTO") PostCreateRequestDTO postCreateRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);



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

    @Operation(summary = "내가 스크랩한 게시글 목록 조회",
            description = """
                로그인한 사용자가 스크랩한 게시글 목록을 타입(free/piece)에 따라 조회합니다.

                - type: 게시글 타입. "free" 또는 "piece"
                - page: 페이지 번호 (0부터 시작)
                - size: 한 페이지에 포함할 게시글 수
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "스크랩한 게시글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(name = "성공 예시", value = """
                            {
                              "status": 200,
                              "code": "GET_SCRAPPED_POST_LIST",
                              "message": "스크랩한 글을 불러왔습니다.",
                              "data": {
                                "totalPost": 510,
                                "size": 1,
                                "page": 0,
                                "responseDTOS": [
                                  {
                                    "id": 532,
                                    "title": "제목 532",
                                    "content": "내용 532",
                                    "thumbImage": null,
                                    "role": "BUSINESS",
                                    "likes": 0,
                                    "scraps": 12,
                                    "comments": 0,
                                    "liked": true,
                                    "scrapped": true,
                                    "hasCommented": false,
                                    "nickname": "길동hong",
                                    "createAt": "2025-06-19"
                                  }
                                ]
                              }
                            }
                                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 게시글 타입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            )
    })
    ResponseEntity<ResponseDTO<PostListResponseDTO>> getScrappedPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);



    @Operation(summary = "내가 작성한 글 조회", description = "게시글 유형(type: free, piece)과 페이지 정보를 기준으로 사용자가 작성한 글 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 작성한 글 목록 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "GET_MY_POST_LIST",
                                      "message": "내가 작성한 글을 불러왔습니다.",
                                      "data": {
                                        "totalPost": 22,
                                        "size": 10,
                                        "page": 0,
                                        "responseDTOS": [
                                          {
                                            "id": 42,
                                            "title": "string",
                                            "content": "string",
                                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/59863c2f-d--.png",
                                            "role": "BUSINESS",
                                            "likes": 0,
                                            "scraps": 12,
                                            "comments": 0,
                                            "liked": true,
                                            "scrapped": true,
                                            "hasCommented": false,
                                            "nickname": "길동hong",
                                            "createAt": "2025-06-19"
                                          }
                                        ]
                                      }
                                    }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 게시글 타입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE)
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            )
    })
    ResponseEntity<ResponseDTO<PostListResponseDTO>> getMyPosts(
            @RequestParam String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);



    @Operation(summary = "인기 게시글 상위 2개 조회", description = "최근 12시간 이내 작성된 게시글 중 좋아요+스크랩 기준으로 상위 2개를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인기 게시글 상위 2개 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_GET_HOT_POSTS",
                                              "message": "인기 게시글 상위 2개 조회했습니다.",
                                              "data": [
                                                  {
                                                    "id": 21,
                                                    "title": "string",
                                                    "content": "string",
                                                    "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/69196aa6-6--.png",
                                                    "role": "BUSINESS",
                                                    "likes": 0,
                                                    "scraps": 0,
                                                    "comments": 0,
                                                    "nickname": "길동hong",
                                                    "createAt": "2025-06-19"
                                                  }
                                              ]
                                            }
                                    """))
            )
    })
    ResponseEntity<ResponseDTO<List<PostPageResponseDTO>>> getHotPosts();

    @Operation(summary = "게시물 조회", description = "게시물을 조회합니다 (type: free/piece)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "선택한 포스트 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 200,
                                      "code": "SUCCESS_GET_POST",
                                      "message": "포스트를 불러왔습니다.",
                                      "data":
                                          {
                                            "id": 42,
                                            "title": "string",
                                            "content": "string",
                                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/59863c2f-d--.png",
                                            "role": "BUSINESS",
                                            "likes": 0,
                                            "scraps": 12,
                                            "comments": 0,
                                            "liked": true,
                                            "scrapped": true,
                                            "hasCommented": false,
                                            "nickname": "길동hong",
                                            "createAt": "2025-06-19",
                                            "imageUrls": ["https://", "https://"]
                                          }
                                    }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 게시글 타입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE))
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = { @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(
                                            name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)})
            )

    })
    ResponseEntity<ResponseDTO<PostReadDetailDTO>> newReadPost(
            @PathVariable String type,
            @PathVariable Long postId);



    @Operation(summary = "포스트 수정 API", description = "기존 게시글을 수정합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포스트 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse",
                                    summary = "성공 응답 예시",
                                    value = """
                        {
                          "status": 200,
                          "code": "SUCCESS_UPDATE_POST",
                          "message": "포스트를 수정했습니다.",
                          "data": {
                            "id": 23,
                            "title": "수정임",
                            "content": "수정임",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "role": "BUSINESS",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "길동hong",
                            "createAt": "2025-06-19",
                            "imageUrls": [
                              "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png"
                            ]
                          }
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "UnauthorizedMember",
                                    summary = "작성자 아님",
                                    value = SwaggerExamples.UNAUTHORIZED_MEMBER
                            )
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(
                                    name = "유저가 존재하지 않음",
                                    value = SwaggerExamples.MEMBER_NOT_EXIST
                            ),
                            @ExampleObject(
                                    name = "글이 존재하지 않음",
                                    value = SwaggerExamples.POST_NOT_EXIST
                            )}
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3에 이미지 등록 실패했을 경우",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED),
                                    @ExampleObject(name = "s3에서 이미지 삭제를 실패한 경우", value = SwaggerExamples.IMAGE_DELETE_FAILED)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<PostReadDetailDTO>> updatePost(
            @PathVariable String type,
            @PathVariable Long postId,
            @RequestPart("updatePostRequestDTO") UpdatePostRequestDTO updatePostRequestDTO,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    );

}
