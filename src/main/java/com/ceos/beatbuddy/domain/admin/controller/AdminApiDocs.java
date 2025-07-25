package com.ceos.beatbuddy.domain.admin.controller;

import com.ceos.beatbuddy.domain.admin.dto.ReportSummaryDTO;
import com.ceos.beatbuddy.domain.member.dto.AdminResponseDto;
import com.ceos.beatbuddy.domain.admin.dto.LoginRequest;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueUpdateDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface AdminApiDocs {
    @Operation(summary = "베뉴 정보 등록", description = "베뉴 정보를 등록합니다.")
    @ApiResponse(
            responseCode = "201", description = "베뉴 정보 등록 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 201,
                              "code": "SUCCESS_CREATE_VENUE",
                              "message": "베뉴 정보가 성공적으로 등록되었습니다.",
                              "data": 1
                            }
                            """))
    )
    @ApiResponse(
            responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = SwaggerExamples.TOO_MANY_IMAGES_5_EXAMPLE))
    )
    @ApiResponse(
            responseCode = "500", description = "이미지 업로드 실패",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = SwaggerExamples.IMAGE_UPLOAD_FAILED))
    )
    @ApiResponse(
            responseCode = "403", description = "권한 없음",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = SwaggerExamples.NOT_ADMIN))
    )
    ResponseEntity<ResponseDTO<Long>> PostVenueInfo(
            @RequestPart(value = "venueRequestDTO") VenueRequestDTO venueRequestDTO,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage,
            @RequestPart(value = "backgroundImage", required = false) List<MultipartFile> backgroundImage
    );

    @Operation(summary = "베뉴 정보 수정", description = """
            베뉴 정보를 수정합니다.
            - `venueRequestDTO`는 수정할 베뉴의 정보를 담고 있습니다.
            - `logoImage`는 베뉴의 로고 이미지 파일입니다. 수정하지 않으려면 생략할 수 있습니다.
            - `backgroundImage`는 베뉴의 배경 이미지 파일 리스트입니다. 수정하지 않으려면 생략할 수 있습니다.
            - `deleteImageUrls`는 삭제할 이미지 URL 리스트입니다. 수정하지 않으려면 생략할 수 있습니다.
            """)
    @ApiResponse(
            responseCode = "200", description = "베뉴 정보 수정 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = """
                            {
                              "status": 200,
                              "code": "SUCCESS_UPDATE_VENUE",
                              "message": "베뉴 정보가 성공적으로 수정되었습니다.",
                              "data": "수정이 완료되었습니다."
                            }
                            """))
    )
    @ApiResponse(
            responseCode = "400", description = "잘못된 요청",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = SwaggerExamples.TOO_MANY_IMAGES_5_EXAMPLE))
    )
    @ApiResponse(
            responseCode = "404", description = "리소스 없음",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = {@ExampleObject(name = "베뉴 정보 없음", value = SwaggerExamples.VENUE_NOT_EXIST),
                            @ExampleObject(name = "이미지 없음", value = SwaggerExamples.NOT_FOUND_IMAGE)})
    )
    @ApiResponse(
            responseCode = "500", description = "서버 오류",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = {@ExampleObject(name = "이미지 업로드 실패", value = SwaggerExamples.IMAGE_UPLOAD_FAILED),
                            @ExampleObject(name = "이미지 삭제 실패", value = SwaggerExamples.IMAGE_DELETE_FAILED)})
    )
    @ApiResponse(
            responseCode = "403", description = "권한 없음",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class),
                    examples = @ExampleObject(value = SwaggerExamples.NOT_ADMIN))
    )
    ResponseEntity<ResponseDTO<String>> updateVenueInfo(
            @PathVariable Long venueId,
            @RequestPart(value = "venueRequestDTO") VenueUpdateDTO venueUpdateDTO,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage,
            @RequestPart(value = "backgroundImage", required = false) List<MultipartFile> backgroundImage
    );

    @Operation(summary = "id를 통한 토큰 발급", description = "기존에 생성된 id를 통해 토큰을 발급받습니다.")
    @Parameter(description = "미리 생성된 id"
            , content = @Content(mediaType = "text/plain")
            , schema = @Schema(implementation = LoginRequest.class))
    @ApiResponse(responseCode = "200", description = "로그인 성공",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AdminResponseDto.class)))
    ResponseEntity<AdminResponseDto> login(@RequestBody LoginRequest request);

    @Operation(summary = "베뉴 정보 삭제", description = "베뉴 정보를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "베뉴 정보 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "베뉴 정보가 존재하지 않음")
    })
    ResponseEntity<Long> DeleteVenueInfo(@PathVariable Long venueId);


    @Operation(summary = "신고 목록 조회", description = "신고된 게시글의 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "신고 목록 조회 성공",
            content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = """
                    {
                      "status": 200,
                      "code": "SUCCESS_GET_REPORT_LIST",
                      "message": "신고 목록을 성공적으로 조회했습니다.",
                      "data": [
                        {
                          "reportId": 1,
                          "targetType": "EVENT_COMMENT",
                          "targetId": 1,
                          "targetTitle": "제목",
                          "targetContent": "내용",
                          "reason": "에바임",
                          "reporterId": 156,
                          "reporterNickname": "요시",
                          "reportedAt": "2025-07-10T00:37:47.3967"
                        }
                      ]
                    }
                    """))
    )
    @ApiResponse(responseCode = "400", description = "어드민이 아닌 사용자",
            content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = """
            {
              "status": 400,
              "error": "BAD_REQUEST",
              "code": "NOT_ADMIN",
              "message": "해당 계정은 어드민이 아닙니다."
            }
            """))
    )
    ResponseEntity<ResponseDTO<List<ReportSummaryDTO>>> getAllReports();

    @Operation(summary = "신고 삭제", description = "신고를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신고 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "어드민이 아닌 사용자",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "NOT_ADMIN",
                                      "message": "해당 계정은 어드민이 아닙니다."
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "신고가 존재하지 않음")
    })
    ResponseEntity<ResponseDTO<String>> deleteReport(@PathVariable Long reportId);

    @Operation(summary = "신고 처리", description = "신고를 처리합니다. 신고 대상의 원글을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "신고 처리 성공"),
            @ApiResponse(responseCode = "400", description = "어드민이 아닌 사용자",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = """
                                    {
                                      "status": 400,
                                      "error": "BAD_REQUEST",
                                      "code": "NOT_ADMIN",
                                      "message": "해당 계정은 어드민이 아닙니다."
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "리소스 없음",
                    content = @Content(mediaType = "application/json",
                            examples = {@ExampleObject(value = SwaggerExamples.REPORT_NOT_FOUND),
                                    @ExampleObject(value = SwaggerExamples.TARGET_NOT_FOUND)}))
    })
    ResponseEntity<ResponseDTO<String>> processReport(@PathVariable Long reportId);
}
