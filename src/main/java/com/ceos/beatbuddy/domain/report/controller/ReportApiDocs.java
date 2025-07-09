package com.ceos.beatbuddy.domain.report.controller;

import com.ceos.beatbuddy.domain.report.dto.ReportRequestDTO;
import com.ceos.beatbuddy.global.SwaggerExamples;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

public interface ReportApiDocs {
    /**
     * 신고를 접수하는 API
     * @param reportRequestDTO 신고 요청 DTO
     * @return ResponseEntity<ResponseDTO<String>> 신고 접수 결과
     */
    @Operation(summary = "신고 접수 API", description = """
            신고를 접수하는 API
            - ReportTargetType은 FREE_POST, PIECE_POST, EVENT, VENUE, FREE_POST_COMMENT, EVENT_COMMENT, VENUE_COMMENT 중 하나입니다.
            - targetId는 신고 대상의 ID입니다. 글이나, 댓글의 ID를 입력해야 합니다.
            - reason은 신고 사유입니다. 1자 이상 100자 이하의 문자열이어야 합니다.
            """)
    @ApiResponse(
            responseCode = "201",
            description = "신고가 접수되었습니다.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "신고 접수 성공 예시",
                            value = """
                                    {
                                        "status": 201,
                                        "code": "SUCCESS_CREATED_REPORT",
                                        "message": "신고가 성공적으로 접수되었습니다.",
                                        "data": "신고가 접수되었습니다."
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "신고 대상이 존재하지 않습니다.",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "신고 대상 미존재 예시",
                            value = SwaggerExamples.TARGET_NOT_FOUND)
            )
    )
    ResponseEntity<ResponseDTO<String>> submitReport(
            @Valid @RequestBody ReportRequestDTO reportRequestDTO);
}
