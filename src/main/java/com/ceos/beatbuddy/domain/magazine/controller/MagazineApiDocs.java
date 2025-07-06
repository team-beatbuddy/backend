package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazinePageResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
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

public interface MagazineApiDocs {
    @Operation(summary = "홈에 보이는 매거진, 작성 기능\n",
            description = "admin과 business 멤버에 한해서만 매거진을 작성할 수 있도록 해두었습니다. (추후 변경 가능), 데이터 전달은 multipart/form-data이며, \n" +
                    "        'magazineRequestDTO'는 JSON 문자열 형태로 전송해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "매거진이 성공적으로 작성되었습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATED_MAGAZINE",
                              "message": "매거진이 성공적으로 작성되었습니다.",
                              "data": {
                                "magazineId": 5,
                                "title": "string",
                                "content": "string",
                                "writerId": 156,
                                "imageUrls": [
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250702_035225_b7c88a0b-f4e0-490a-a318-fd29380b12c1.png"
                                ],
                                "views": 0,
                                "likes": 0,
                                "createdAt": "2025-07-02T03:52:26.5691433",
                                "isLiked": false
                              }
                            }
                                    """)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 유저",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                                    @ExampleObject(name = "존재하지 않는 유저", value = SwaggerExamples.MEMBER_NOT_EXIST)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 요청 (권한이 없는 일반 유저)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "권한 없는 유저",
                                            value = SwaggerExamples.CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER)
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);

    @Operation(summary = "홈에 보이는 매거진, 조회 기능\n",
            description = "매거진 리스트 조회 기능")
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "매거진 리스트 조회 성공",
                                            value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                                              "message": "매거진이 성공적으로 불러왔습니다.",
                                              "data": [
                                                {
                                                  "magazineId": 1,
                                                  "thumbImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png",
                                                  "title": "제목",
                                                  "content": "내용",
                                                  "liked": false
                                                }
                                              ]
                                            }
                                            """
                                    ),
                                    @ExampleObject(name = "빈 매거진 리스트", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
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
            )
    })
    ResponseEntity<ResponseDTO<List<MagazineHomeResponseDTO>>> readMagazineList();

    @Operation(summary = "매거진 전체 조회\n",
            description = "매거진 전체 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = {
                                    @ExampleObject(
                                            name = "매거진 전체 조회 성공",
                                            value = """
                                            {
                                              "status": 200,
                                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                                              "message": "매거진을 성공적으로 불러왔습니다.",
                                              "data": {
                                                "page": 1,
                                                "size": 10,
                                                "totalCount": 5,
                                                "magazines": [
                                                  {
                                                    "magazineId": 5,
                                                    "title": "string",
                                                    "content": "string",
                                                    "writerId": 156,
                                                    "imageUrls": [
                                                      "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250702_035225_b7c88a0b-f4e0-490a-a318-fd29380b12c1.png"
                                                    ],
                                                    "views": 0,
                                                    "likes": 0,
                                                    "createdAt": "2025-07-02T03:52:26.569143",
                                                    "liked": false
                                                  },
                                                  {
                                                    "magazineId": 4,
                                                    "title": "string",
                                                    "content": "string",
                                                    "writerId": 156,
                                                    "imageUrls": [
                                                      "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/magazine/20250702_035127_a96f05c4-441c-49be-9b41-83d1b7b2aa08.png"
                                                    ],
                                                    "views": 0,
                                                    "likes": 0,
                                                    "createdAt": "2025-07-02T03:51:28.732743",
                                                    "liked": false
                                                  },
                                                  {
                                                    "magazineId": 3,
                                                    "title": "string",
                                                    "content": "string",
                                                    "writerId": 156,
                                                    "imageUrls": [],
                                                    "views": 0,
                                                    "likes": 0,
                                                    "createdAt": "2025-07-02T03:35:20.952583",
                                                    "liked": false
                                                  },
                                                  {
                                                    "magazineId": 2,
                                                    "title": "string",
                                                    "content": "string",
                                                    "writerId": 156,
                                                    "imageUrls": [],
                                                    "views": 0,
                                                    "likes": 0,
                                                    "createdAt": "2025-07-02T03:32:48.383555",
                                                    "liked": false
                                                  },
                                                  {
                                                    "magazineId": 1,
                                                    "title": "제목",
                                                    "content": "내용",
                                                    "writerId": 156,
                                                    "imageUrls": [
                                                      "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                                    ],
                                                    "views": 0,
                                                    "likes": 0,
                                                    "createdAt": "2025-06-12T14:05:40.216235",
                                                    "liked": false
                                                  }
                                                ]
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(name = "빈 매거진 리스트", value = SwaggerExamples.SUCCESS_BUT_EMPTY_LIST)
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (페이지 번호가 0 이하인 경우)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "잘못된 페이지 요청", value = SwaggerExamples.PAGE_OUT_OF_BOUNDS)
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
            )
    })
    ResponseEntity<ResponseDTO<MagazinePageResponseDTO>> readAllMagazines(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    );

    @Operation(summary = "매거진 상세 조회\n",
            description = "매거진 상세 조회")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                              "message": "매거진이 성공적으로 불러왔습니다.",
                              "data": {
                                "magazineId": 1,
                                "title": "제목",
                                "content": "내용",
                                "memberId": 156,
                                "imageUrls": [
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                ],
                                "views": 0,
                                "likes": 0
                              }
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
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> readDetailMagazine(@PathVariable Long magazineId);

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
                              "data": {
                                "magazineId": 1,
                                "title": "제목",
                                "content": "내용",
                                "memberId": 156,
                                "imageUrls": [
                                  "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                ],
                                "views": 0,
                                "likes": 1
                              }
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
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> likeMagazine(@PathVariable Long magazineId);

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
                                          "data": {
                                            "magazineId": 1,
                                            "title": "제목",
                                            "content": "내용",
                                            "memberId": 156,
                                            "imageUrls": [
                                              "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                            ],
                                            "views": 0,
                                            "likes": 0
                                          }
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
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> deleteLikeMagazine(@PathVariable Long magazineId);
}
