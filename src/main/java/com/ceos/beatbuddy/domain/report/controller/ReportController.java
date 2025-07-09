package com.ceos.beatbuddy.domain.report.controller;

import com.ceos.beatbuddy.domain.report.dto.ReportRequestDTO;
import com.ceos.beatbuddy.domain.report.service.ReportService;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reports")
public class ReportController implements ReportApiDocs {
    private final ReportService reportService;

    // 신고 접수 API
    /**
     * 신고를 접수하는 API
     * @param reportRequestDTO 신고 요청 DTO
     * @return ResponseEntity<ResponseDTO<String>> 신고 접수 결과
     */
    @Override
    @PostMapping("/submit")
    public ResponseEntity<ResponseDTO<String>> submitReport(
            @Valid @RequestBody ReportRequestDTO reportRequestDTO) {
        Long reporterId = SecurityUtils.getCurrentMemberId();
        reportService.submitReport(reportRequestDTO, reporterId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATED_REPORT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATED_REPORT, "신고가 접수되었습니다."))
                ;
    }


}
