package com.ceos.beatbuddy.domain.post.controller;

import com.ceos.beatbuddy.domain.post.dto.*;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
    @Operation(summary = "#####게시물 생성 - 새로운 버전",
            description = """
            게시물을 생성합니다 (type: free/piece)
            - 공통으로는 title(옵션), content(필수), anonymous 입니다.
            - free: hashtag (압구정로데오/홍대/이태원/강남.신사/뮤직/자유/번개 모임/International/19+/LGBTQ/짤.밈) 중 하나입니다.
            - piece: totalPrice, totalMembers, eventDate, venueId
            """
    )
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
                                "createAt": "2025-07-06T22:19:08.514011",
                                "nickname": "길동hong"
                              }
                            }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE),
                                        @ExampleObject(name = "중복된 해시태그", value = SwaggerExamples.DUPLICATED_HASHTAG),
                                    @ExampleObject(name = "필수 입력 내용을 입력하지 않았을 경우", value = """
                                            {
                                              "status": 400,
                                              "error": "BAD_REQUEST",
                                              "code": "BAD_REQUEST_VALIDATION",
                                              "message": "요청 값이 유효하지 않습니다.",
                                              "errors": {
                                                "content": "내용은 1자 이상 1000자 이하로 작성해주세요."
                                              }
                                            }
                                          """),
                                    @ExampleObject(name = "이미지 최대 20장", value = SwaggerExamples.TOO_MANY_IMAGES_20_EXAMPLE),
                            }
                    )
            ),
            @ApiResponse(responseCode = "404", description = "리소스 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 베뉴", value = SwaggerExamples.VENUE_NOT_EXIST),
                                    @ExampleObject(name = "존재하지 않는 해시태그", value = SwaggerExamples.NOT_FOUND_HASHTAG)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 에러",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED),
                                    @ExampleObject(name = "Elasticsearch에 게시글 등록을 실패했을 경우", value = SwaggerExamples.ELASTICSEARCH_POST_CREATE_FAILED)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<ResponsePostDto>> addNewPost(
            @PathVariable String type,
            @Valid
            @RequestPart("postCreateRequestDTO") PostCreateRequestDTO postCreateRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);



    @Operation(summary = "내가 스크랩한 게시글 목록 조회",
            description = """
                로그인한 사용자가 스크랩한 게시글 목록을 타입(free/piece)에 따라 조회합니다.

                - type: 게시글 타입. "free" 또는 "piece"
                - page: 페이지 번호 (1부터 시작)
                - size: 한 페이지에 포함할 게시글 수
                
                - 익명인 경우, 프로필 사진이 "" 입니다.
                - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
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
                                "totalPost": 2,
                                "size": 10,
                                "page": 0,
                                "responseDTOS": [
                                  {
                                    "id": 537,
                                    "title": "string",
                                    "content": "string",
                                    "role": "BUSINESS",
                                    "likes": 1,
                                    "scraps": 1,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": true,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-07-06T22:19:08.514011",
                                    "hashtags": [
                                      "이태원",
                                      "홍대",
                                      "강남.신사"
                                    ],
                                    "isAuthor": true,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": false
                                  },
                                  {
                                    "id": 539,
                                    "title": "string",
                                    "content": "string",
                                    "role": "BUSINESS",
                                    "likes": 1,
                                    "scraps": 1,
                                    "comments": 1,
                                    "liked": true,
                                    "scrapped": true,
                                    "hasCommented": true,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-07-06T22:19:08.514011",
                                    "hashtags": [
                                      "홍대",
                                      "이태원",
                                      "뮤직"
                                    ],
                                    "isAuthor": true,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": false
                                  }
                                ]
                              }
                            }
                                """)
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE),
                                    @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS)}
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);



    @Operation(summary = "내가 작성한 글 조회", description = """
    게시글 유형(type: free, piece)과 페이지 정보를 기준으로 사용자가 작성한 글 목록을 조회합니다.
    
    - 익명인 경우, 프로필 사진이 "" 입니다.
    - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
    
    """)
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
                                "totalPost": 46,
                                "size": 10,
                                "page": 1,
                                "responseDTOS": [
                                  {
                                    "id": 537,
                                    "title": "string",
                                    "content": "string",
                                    "role": "BUSINESS",
                                    "likes": 1,
                                    "scraps": 1,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": true,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-07-06T22:19:08.514011",
                                    "hashtags": [
                                      "이태원",
                                      "홍대",
                                      "강남.신사"
                                    ],
                                    "isAuthor": true,
                                    "writerId": 1,
                                    "profileImageUrl": "",
                                    "isAnonymous": false
                                  },
                                  {
                                    "id": 41,
                                    "title": "string",
                                    "content": "string",
                                    "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/3d50a441-6--.png",
                                    "role": "ADMIN",
                                    "likes": 0,
                                    "scraps": 0,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": false,
                                    "hasCommented": false,
                                    "nickname": "닉네임 수정1",
                                    "createAt": "2025-07-06T22:19:08.514011",
                                    "hashtags": [
                                      "홍대"
                                    ],
                                    "isAuthor": false,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true
                                  }
                                ]
                              }
                            }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE),
                                    @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS),
                                    @ExampleObject(name = "중복된 해시태그", value = SwaggerExamples.DUPLICATED_HASHTAG)
                            }
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
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);



    @Operation(summary = "인기 게시글 상위 2개 조회", description = """
    
    최근 12시간 이내 작성된 게시글 중 좋아요+스크랩 기준으로 상위 2개를 조회합니다.
    - 익명인 경우, 프로필 사진이 "" 입니다.
    - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
    
    """)
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
                                  "id": 539,
                                  "title": "string",
                                  "content": "string",
                                  "role": "BUSINESS",
                                  "likes": 1,
                                  "scraps": 0,
                                  "comments": 1,
                                  "liked": true,
                                  "scrapped": false,
                                  "hasCommented": true,
                                  "nickname": "BeatBuddy",
                                  "createAt": "2025-07-06T22:19:08.514011",
                                  "hashtags": [
                                    "홍대",
                                    "이태원",
                                    "뮤직"
                                  ],
                                  "isAuthor": false,
                                  "writerId": 1,
                                  "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                  "isAnonymous": true,
                                  "isFollowing": false
                                },
                                {
                                  "id": 538,
                                  "title": "",
                                  "content": "string",
                                  "role": "BUSINESS",
                                  "likes": 1,
                                  "scraps": 0,
                                  "comments": 0,
                                  "liked": false,
                                  "scrapped": false,
                                  "hasCommented": false,
                                  "nickname": "BeatBuddy",
                                  "createAt": "2025-07-06T22:19:08.514011",
                                  "hashtags": [
                                    "홍대"
                                  ],
                                  "isAuthor": false,
                                  "writerId": 1,
                                  "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                  "isAnonymous": true,
                                  "isFollowing": false
                                }
                              ]
                            }
                                    """))
            )
    })
    ResponseEntity<ResponseDTO<List<PostPageResponseDTO>>> getHotPosts();

    @Operation(summary = "게시물 조회", description = """

    게시물을 조회합니다 (type: free/piece)
    - 익명인 경우, 프로필 사진이 "" 입니다.
    - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
    """)
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "선택한 포스트 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = PostListResponseDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_POST",
                      "message": "포스트를 불러왔습니다",
                      "data": {
                        "id": 537,
                        "title": "string",
                        "content": "string",
                        "role": "BUSINESS",
                        "likes": 1,
                        "scraps": 1,
                        "comments": 0,
                        "liked": false,
                        "scrapped": true,
                        "hasCommented": false,
                        "nickname": "BeatBuddy",
                        "createAt": "2025-07-06T22:19:08.514011",
                        "hashtags": [
                          "이태원",
                          "홍대",
                          "강남.신사"
                        ],
                        "isAuthor": true,
                        "imageUrls": ["https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png"],
                        "views": 1,
                        "writerId": 1,
                        "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                        "isAnonymous": true,
                        "isFollowing": false
                      }
                    }
            """))
    ),
    @ApiResponse(responseCode = "400", description = "잘못된 게시글 타입 요청",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE))
    ),
    @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
            content = @Content(mediaType = "application/json",
                    examples = { @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                            @ExampleObject(name = "존재하지 않는 포스트", value = SwaggerExamples.POST_NOT_EXIST)})
    )})
    ResponseEntity<ResponseDTO<PostReadDetailDTO>> newReadPost(
            @PathVariable String type,
            @PathVariable Long postId);



    @Operation(summary = "게시글 수정 API", description = """
                기존 게시글을 수정합니다. 수정되는 필드만 넣으면 됩니다.
                - 만약 title 을 수정하지 않았다면, 넣지 않고 전달하면 됩니다.
                - 해시태그는 기존 해시태그를 지우고, 새로 작성한 해시태그로 덮어씌워집니다.
                
                - 익명인 경우, 프로필 사진이 "" 입니다.
                - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
                """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "포스트 수정 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(
                                    name = "SuccessResponse", summary = "성공 응답 예시",
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
                                        "createAt": "2025-07-06T22:19:08.514011",
                                        "imageUrls": [
                                          "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png"
                                        ],
                                        "hashtags": [
                                          "홍대",
                                          "이태원",
                                          "뮤직"
                                        ],
                                        "views": 1,
                                        "isAuthor": true,
                                        "writerId": 1,
                                        "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                        "isAnonymous": true,
                                        "isFollowing": false
                                      }
                                    }
                        """
                            )
                    )
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE),
                                    @ExampleObject(name = "이미지 최대 20장", value = SwaggerExamples.TOO_MANY_IMAGES_20_EXAMPLE)
                            }
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
                            examples = {@ExampleObject(name = "유저가 존재하지 않음", value = SwaggerExamples.MEMBER_NOT_EXIST),
                            @ExampleObject(name = "글이 존재하지 않음", value = SwaggerExamples.POST_NOT_EXIST)}
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3에 이미지 등록 실패했을 경우 / Elasticsearch 에러",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(name = "s3에 이미지 등록을 실패했을 경우", value = SwaggerExamples.IMAGE_UPLOAD_FAILED),
                                    @ExampleObject(name = "s3에서 이미지 삭제를 실패한 경우", value = SwaggerExamples.IMAGE_DELETE_FAILED),
                                    @ExampleObject(name = "Elasticsearch에 게시글 수정 실패", value = SwaggerExamples.ELASTICSEARCH_POST_CREATE_FAILED)
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

    @Operation(summary = "전체 게시물 조회, 최신순 정렬이 기본입니다.)", description = """
    전체 게시물을 조회합니다 (type: free/piece), (sort: latest)
    - 익명인 경우, 프로필 사진이 "" 입니다.
    - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
    """)
    @ApiResponse(
            responseCode = "200",
            description = "게시글 목록 조회 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_POST_SORT_LIST",
                      "message": "type 에 맞는 post를 불러왔습니다.",
                      "data": {
                        "totalPost": 46,
                        "size": 10,
                        "page": 1,
                        "responseDTOS": [
                          {
                            "id": 539,
                            "title": "제목 수정",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 1,
                            "scraps": 1,
                            "comments": 1,
                            "liked": true,
                            "scrapped": true,
                            "hasCommented": true,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-06T22:19:08.514011",
                            "hashtags": [
                              "홍대",
                              "이태원",
                              "뮤직"
                            ],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": false
                          },
                          {
                            "id": 538,
                            "title": "",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 1,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-06T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": false
                          },
                          {
                            "id": 537,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 1,
                            "scraps": 1,
                            "comments": 0,
                            "liked": false,
                            "scrapped": true,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-06T22:19:08.514011",
                            "hashtags": [
                              "이태원",
                              "홍대",
                              "강남.신사"
                            ],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 536,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-06T22:19:08.514011",
                            "hashtags": [],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 535,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-07-06T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 534,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "",
                            "role": "BUSINESS",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "BeatBuddy",
                            "createAt": "2025-06-30T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": true,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 533,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "",
                            "role": "ADMIN",
                            "likes": 1,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "닉네임 수정1",
                            "createAt": "2025-06-22T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": false,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 42,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/59863c2f-d--.png",
                            "role": "ADMIN",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "닉네임 수정1",
                            "createAt": "2025-06-19T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": false,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 41,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/3d50a441-6--.png",
                            "role": "ADMIN",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "닉네임 수정1",
                            "createAt": "2025-06-19T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": false,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          },
                          {
                            "id": 40,
                            "title": "string",
                            "content": "string",
                            "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/bb16118a-a--.png",
                            "role": "ADMIN",
                            "likes": 0,
                            "scraps": 0,
                            "comments": 0,
                            "liked": false,
                            "scrapped": false,
                            "hasCommented": false,
                            "nickname": "닉네임 수정1",
                            "createAt": "2025-06-19T22:19:08.514011",
                            "hashtags": [
                              "홍대"
                            ],
                            "isAuthor": false,
                            "writerId": 1,
                            "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                            "isAnonymous": true,
                            "isFollowing": true
                          }
                        ]
                      }
                    }
        """)
            )
    )
    ResponseEntity<ResponseDTO<PostListResponseDTO>> readAllPostsSort(
            @PathVariable String type,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size);



    @Operation(summary = "해시태그로 게시글 목록 조회", description = """
            해시태그로 게시글 목록을 조회합니다.
            - hashtags: (압구정로데오/홍대/이태원/강남.신사/뮤직/자유/번개 모임/International/19+/LGBTQ/짤.밈) 중 하나입니다.
            - hashtags는 중복되지 않도록 입력해주세요.
            
            - 익명인 경우, 프로필 사진이 "" 입니다.
            - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
            """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "해시태그에 해당하는 포스트 목록을 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_POST_LIST_BY_HASHTAG",
                              "message": "해시태그에 해당하는 포스트 목록을 성공적으로 조회했습니다.",
                              "data": {
                                "totalPost": 2,
                                "size": 10,
                                "page": 0,
                                "responseDTOS": [
                                  {
                                    "id": 539,
                                    "title": "제목 수정",
                                    "content": "string",
                                    "thumbImage": "",
                                    "role": "BUSINESS",
                                    "likes": 1,
                                    "scraps": 1,
                                    "comments": 1,
                                    "liked": true,
                                    "scrapped": true,
                                    "hasCommented": true,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-06-19T22:19:08.514011",
                                    "hashtags": [
                                      "홍대",
                                      "이태원",
                                      "뮤직"
                                    ],
                                    "isAuthor": true,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true,
                                    "isFollowing": true
                                  },
                                  {
                                    "id": 537,
                                    "title": "string",
                                    "content": "string",
                                    "thumbImage": "",
                                    "role": "BUSINESS",
                                    "likes": 1,
                                    "scraps": 1,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": true,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-06-19T22:19:08.514011",
                                    "hashtags": [
                                      "이태원",
                                      "홍대",
                                      "강남.신사"
                                    ],
                                    "isAuthor": true,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true,
                                    "isFollowing": true
                                  }
                                ]
                              }
                            }
                            """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "중복된 해시태그", value = SwaggerExamples.DUPLICATED_HASHTAG),
                                        @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS)}
                    )
            ),
            @ApiResponse(responseCode = "404", description = "해시태그가 존재하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "존재하지 않는 해시태그", value = SwaggerExamples.NOT_FOUND_HASHTAG),
                                        @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<PostListResponseDTO>> hashTagPostList(
            @RequestParam List<String> hashtags,
            @Parameter(description = "페이지 번호")
            @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "페이지 당 요청할 게시물 개수")
            @RequestParam(defaultValue = "10") int size);


    @Operation(summary = "사용자 게시글 조회", description = """
    특정 사용자의 게시글을 조회합니다. 익명으로 작성한 글은 뜨지 않습니다.
    - 익명인 경우, 프로필 사진이 "" 입니다.
    - 프로필 사진이 설정되지 않은 경우에도 프로필 사진이 "" 입니다.
    """)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "사용자 게시글 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PostListResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "GET_USER_POST_LIST",
                              "message": "사용자가 작성한 포스트 목록을 성공적으로 조회했습니다.",
                              "data": {
                                "totalPost": 3,
                                "size": 10,
                                "page": 1,
                                "responseDTOS": [
                                  {
                                    "id": 536,
                                    "title": "string",
                                    "content": "string",
                                    "thumbImage": "",
                                    "role": "BUSINESS",
                                    "likes": 0,
                                    "scraps": 0,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": false,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-06-19T22:19:08.514011",
                                    "hashtags": [],
                                    "isAuthor": false,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true,
                                    "isFollowing": true
                                  },
                                  {
                                    "id": 535,
                                    "title": "string",
                                    "content": "string",
                                    "thumbImage": "",
                                    "role": "BUSINESS",
                                    "likes": 0,
                                    "scraps": 0,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": false,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-06-19T22:19:08.514011",
                                    "hashtags": [
                                      "홍대"
                                    ],
                                    "isAuthor": false,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true,
                                    "isFollowing": true
                                  },
                                  {
                                    "id": 534,
                                    "title": "string",
                                    "content": "string",
                                    "thumbImage": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/post/20250704_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "role": "BUSINESS",
                                    "likes": 0,
                                    "scraps": 0,
                                    "comments": 0,
                                    "liked": false,
                                    "scrapped": false,
                                    "hasCommented": false,
                                    "nickname": "BeatBuddy",
                                    "createAt": "2025-06-19T22:19:08.514011",
                                    "hashtags": [
                                      "홍대"
                                    ],
                                    "isAuthor": false,
                                    "writerId": 1,
                                    "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/profile/20250622_154930_3f228896-4a44-45a2-bccf-66ed9b7e966b.png",
                                    "isAnonymous": true,
                                    "isFollowing": true
                                  }
                                ]
                              }
                            }
                            """))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 요청",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "잘못된 type 예시", value = SwaggerExamples.INVALID_POST_TYPE),
                                        @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS)}
                    )
            ),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            )
    })
    ResponseEntity<ResponseDTO<PostListResponseDTO>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam String type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size);


    @Operation(summary = "게시글 삭제 API", description = """
    게시글을 삭제합니다. 작성자만 삭제할 수 있습니다.
    - type(free/piece) 에 따라 삭제할 수 있는 게시글이 다릅니다.
    - 현재는 free 만 삭제할 수 있습니다.
    """)
    @ApiResponse(
            responseCode = "200",
            description = "게시글 삭제 성공",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_DELETE_POST",
                              "message": "포스트를 삭제했습니다.",
                              "data": "게시글이 삭제되었습니다."
                            }
                    """))
    )
    @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(name = "UnauthorizedMember", value = SwaggerExamples.UNAUTHORIZED_MEMBER)
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "존재하지 않는 유저 또는 게시글",
            content = @Content(
                    mediaType = "application/json",
                    examples = {
                            @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST),
                            @ExampleObject(name = "존재하지 않는 게시글", value = SwaggerExamples.POST_NOT_EXIST)
                    }
            )
    )
    ResponseEntity<ResponseDTO<String>> newDeletePost(@PathVariable String type,
                                                      @PathVariable Long postId);


}
