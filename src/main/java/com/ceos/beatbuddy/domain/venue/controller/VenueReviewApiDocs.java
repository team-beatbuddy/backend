package com.ceos.beatbuddy.domain.venue.controller;


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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface VenueReviewApiDocs {
    @Operation(
            summary = "베뉴 리뷰 작성하기",
            description = "베뉴 리뷰를 작성합니다. 리뷰 작성 시 이미지가 5개까지 첨부 가능합니다. " +
                    "이미지는 필수 사항이 아니며, 첨부하지 않아도 리뷰 작성이 가능합니다. " +
                    "리뷰 내용은 최대 400자까지 입력할 수 있습니다. " +
                    "multipart/form-data 형식으로 요청해야 하며, 파일 1개의 최대 크기는 10MB입니다. " +
                    "전체 요청 크기는 30MB를 초과할 수 없습니다. "
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
                                "venueReviewId": 6,
                                "content": "string",
                                "nickname": "길동hong",
                                "likes": 0,
                                "liked": false,
                                "profileImageUrl": "https://beatbuddy.s3.ap-northeast-2.amazonaws.com/member/01e2e094-3--.png",
                                "role": "BUSINESS",
                                "createdAt": "2025-07-01T19:53:56.294687",
                                "imageUrls": [
                                  "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/review/20250701_195355_32a97151-fdb7-4334-9d20-e508f3a48fb6.png",
                                  "https://beatbuddy-venue.s3.ap-northeast-2.amazonaws.com/review/20250701_195355_3d34539f-7521-4bdb-ae4c-d04efbecfe9f.png"
                                ],
                                "anonymous": false
                              }
                            }
                    """))
            ),
            @ApiResponse(responseCode = "400", description = "필드 정보 누락, 이미지 개수 초과, 글자수 초과",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {@ExampleObject(name = "필드 정보 누락", value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "BAD_REQUEST_VALIDATION",
                                      "message": "요청 값이 유효하지 않습니다.",
                                      "errors": {
                                        "content": "리뷰 내용은 필수입니다."
                                      }
                                    }
                                    """),
                            @ExampleObject(name = "이미지 개수 초과", value = SwaggerExamples.TOO_MANY_IMAGES_5_EXAMPLE),
                            @ExampleObject(name = "글자수 초과", value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "BAD_REQUEST_VALIDATION",
                                      "message": "요청 값이 유효하지 않습니다.",
                                      "errors": {
                                        "content": "리뷰 내용은 400자까지만 입력 가능합니다."
                                      }
                                    }
                                    """)
                            }
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
