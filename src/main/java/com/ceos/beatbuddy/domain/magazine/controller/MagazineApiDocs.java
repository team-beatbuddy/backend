package com.ceos.beatbuddy.domain.magazine.controller;

import com.ceos.beatbuddy.domain.magazine.dto.MagazineDetailDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineHomeResponseDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineRequestDTO;
import com.ceos.beatbuddy.domain.magazine.dto.MagazineResponseDTO;
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
import jakarta.websocket.server.PathParam;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MagazineApiDocs {
    @Operation(summary = "홈에 보이는 매거진, 작성 기능\n",
            description = "admin과 business 멤버에 한해서만 매거진을 작성할 수 있도록 해두었습니다. (추후 변경 가능)")
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
                                            "magazineId": 1,
                                            "title": "제목",
                                            "content": "내용",
                                            "imageUrls": [
                                              "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                            ],
                                            "createdAt": "2025-06-12T14:05:40.216235",
                                            "writerId": 156
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
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "잘못된 요청 (권한이 없는 일반 유저)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "권한 없는 유저",
                                            value = """
                                {
                                  "status": 403,
                                  "error": "UNAUTHORIZED",
                                  "code": "CANNOT_ADD_MAGAZINE_UNAUTHORIZED_MEMBER",
                                  "message": "글을 작성할 수 없는 유저입니다."
                                }
                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineResponseDTO>> addMagazine(
            @Valid @RequestPart("magazineRequestDTO") MagazineRequestDTO magazineRequestDTO,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);

    @Operation(summary = "홈에 보이는 매거진, 조회 기능\n",
            description = "매거진 리스트 조회 기능")
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
                                  "data": [
                                    {
                                      "magazineId": 1,
                                      "thumbImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png"
                                    }
                                  ]
                                }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다. 하지만 비어있음.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_BUT_EMPTY_LIST",
                              "message": "성공적으로 조회했으나 리스트가 비었습니다.",
                              "data": []
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
    ResponseEntity<ResponseDTO<List<MagazineHomeResponseDTO>>> readMagazineList();


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
                                "scraps": 0,
                                "views": 0,
                                "likes": 0
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 매거진",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(
                                    name = "존재하지 않는 매거진",
                                    value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MAGAZINE_NOT_EXIST",
                                  "message": "해당 매거진을 찾을 수 없습니다."
                                }
                            """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> readDetailMagazine(@PathVariable Long magazineId);

    @Operation(summary = "매거진 스크랩\n",
            description = "매거진을 스크랩합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진을 스크랩합니다.",
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
                                "scraps": 1,
                                "views": 0,
                                "likes": 0
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 매거진",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(
                                    name = "존재하지 않는 매거진",
                                    value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MAGAZINE_NOT_EXIST",
                                  "message": "해당 매거진을 찾을 수 없습니다."
                                }
                            """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<MagazineDetailDTO>> scrapMagazine(@PathVariable Long magazineId);

    @Operation(summary = "매거진 스크랩\n",
            description = "매거진을 스크랩합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "매거진을 스크랩합니다.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_GET_MAGAZINE_LIST",
                              "message": "매거진이 성공적으로 불러왔습니다.",
                              "data": [
                                {
                                  "magazineId": 1,
                                  "thumbImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/ab37ac94-4Group%201000003259.png",
                                  "title": "제목"
                                }
                              ]
                            }
                                    """)
                    )
            ),
            @ApiResponse(responseCode = "200", description = "매거진 리스트를 성공적으로 조회했습니다. 하지만 비어있음.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_BUT_EMPTY_LIST",
                              "message": "성공적으로 조회했으나 리스트가 비었습니다.",
                              "data": []
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
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "존재하지 않는 매거진",
                    content = @Content(
                            mediaType = "application/json",
                            examples =
                            @ExampleObject(
                                    name = "존재하지 않는 매거진",
                                    value = """
                                {
                                  "status": 404,
                                  "error": "NOT_FOUND",
                                  "code": "MAGAZINE_NOT_EXIST",
                                  "message": "해당 매거진을 찾을 수 없습니다."
                                }
                            """
                            )
                    )
            )
    })
    ResponseEntity<ResponseDTO<List<MagazineHomeResponseDTO>>> getScrapMagazineList();
}
