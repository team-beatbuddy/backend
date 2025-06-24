package com.ceos.beatbuddy.domain.venue.controller;


import com.ceos.beatbuddy.domain.post.dto.PostListResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewResponseDTO;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VenueReviewApiDocs {
    @Operation(
            summary = "베뉴 리뷰 작성하기",
            description = "베뉴 리뷰를 작성합니다. 리뷰 작성 시 이미지 첨부가 가능합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "베뉴 리뷰 작성 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ResponseDTO.class),
                            examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATE_VENUE_REVIEW",
                              "message": "베뉴 리뷰를 작성했습니다.",
                              "data": {
                                "venueReviewId": 1,
                                "content": "string",
                                "nickname": "길동hong",
                                "views": 0,
                                "likes": 0,
                                "liked": false,
                                "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/member/01e2e094-3--.png",
                                "role": "BUSINESS",
                                "createdAt": "2025-06-24T16:35:36.9220865",
                                "anonymous": false
                              }
                            }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "필드 정보 누락",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(name = "필드 정보 누락", value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "BAD_REQUEST_VALIDATION",
                                      "message": "요청 값이 유효하지 않습니다.",
                                      "errors": {
                                        "content": "리뷰 내용은 필수입니다."
                                      }
                                    }
                                    """)
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
    ResponseEntity<ResponseDTO<VenueReviewResponseDTO>> createVenueReview(
            @PathVariable Long venueId,
            @RequestPart VenueReviewRequestDTO dto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images);
}
