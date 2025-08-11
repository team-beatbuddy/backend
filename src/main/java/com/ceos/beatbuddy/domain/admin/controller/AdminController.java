package com.ceos.beatbuddy.domain.admin.controller;

import com.ceos.beatbuddy.domain.admin.application.AdminService;
import com.ceos.beatbuddy.domain.admin.dto.LoginRequest;
import com.ceos.beatbuddy.domain.admin.dto.ReportSummaryDTO;
import com.ceos.beatbuddy.domain.member.application.BusinessMemberService;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.dto.AdminMemberListDTO;
import com.ceos.beatbuddy.domain.member.dto.AdminResponseDto;
import com.ceos.beatbuddy.domain.report.service.ReportService;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueUpdateDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {
    private final VenueInfoService venueInfoService;
    private final AdminService adminService;
    private final ReportService reportService;
    private final MemberService memberService;
    private final BusinessMemberService businessMemberService;

    // 베뉴 등록
    @Override
    @PostMapping(value = "/venue", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<Long>> PostVenueInfo(
            @RequestPart(value = "venueRequestDTO") VenueRequestDTO venueRequestDTO,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage,
            @RequestPart(value = "backgroundImage", required = false) List<MultipartFile> backgroundImage
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        Long venueId = venueInfoService.addVenueInfo(venueRequestDTO, logoImage, backgroundImage, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATE_VENUE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATE_VENUE, venueId));
    }


    @Override
    @DeleteMapping("/{venueId}")
    public ResponseEntity<Long> DeleteVenueInfo(@PathVariable Long venueId) {
        return ResponseEntity.ok(venueInfoService.deleteVenueInfo(venueId));
    }


    @Override
    @PutMapping(value = "/{venueId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ResponseDTO<String>> updateVenueInfo(
            @PathVariable Long venueId,
            @RequestPart(value = "venueRequestDTO") VenueUpdateDTO venueUpdateDTO,
            @RequestPart(value = "logoImage", required = false) MultipartFile logoImage,
            @RequestPart(value = "backgroundImage", required = false) List<MultipartFile> backgroundImage
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        venueInfoService.updateVenueInfo(venueId, venueUpdateDTO, logoImage, backgroundImage, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE_VENUE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE_VENUE, "수정이 완료되었습니다."));
    }


    @PostMapping("/join")
    public ResponseEntity<String> join(@RequestBody String id) {
        Long adminId = adminService.createAdmin(id);

        String result = "id : " + id;
        return ResponseEntity.ok(result + "\n join success!\n");
    }


    @Override
    @PostMapping("/login")
    public ResponseEntity<AdminResponseDto> login(@RequestBody LoginRequest request) {
        Long adminId = adminService.findAdmin(request.getId());
        return adminService.createAdminToken(adminId, request.getId());
    }

    @Override
    @GetMapping("/report")
    public ResponseEntity<ResponseDTO<List<ReportSummaryDTO>>> getAllReports() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<ReportSummaryDTO> reports = reportService.getAllReports(memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_REPORT_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_REPORT_LIST, reports));
    }
    @Override
    @DeleteMapping("/report/{reportId}")
    public ResponseEntity<ResponseDTO<String>> deleteReport(
            @PathVariable Long reportId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        reportService.deleteReport(reportId, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_DELETE_REPORT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_DELETE_REPORT, "신고가 삭제되었습니다."));
    }

    // 신고 처리 후 삭제
    @Override
    @DeleteMapping("/report/{reportId}/process")
    public ResponseEntity<ResponseDTO<String>> processReport(
            @PathVariable Long reportId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        reportService.processReport(reportId, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_PROCESS_REPORT.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_PROCESS_REPORT, "신고가 처리되었습니다."));
    }


    @Operation(
            summary = "모든 멤버 정보 조회(관리자 전용)",
            description = "관리자가 모든 멤버의 정보를 조회합니다. 이 API는 관리자 권한이 필요합니다."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "모든 멤버 정보 조회 성공",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AdminMemberListDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한이 없는 사용자: 관리자 권한이 필요합니다.",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @GetMapping("/members")
    public ResponseEntity<ResponseDTO<List<AdminMemberListDTO>>> getMemberInfo(
            @Parameter(description = "멤버의 역할 필터링 (예: ADMIN, BUSINESS, BUSINESS_NOT 등)", required = false)
            @RequestParam(required = false) String role) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<AdminMemberListDTO> result = memberService.getAllMembers(memberId, role);
        return ResponseEntity.ok(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MEMBER_INFO, result));
    }

    @Operation(
            summary = "비즈니스 멤버 승인",
            description = "관리자가 비즈니스 멤버를 승인합니다. 이 API는 관리자 권한이 필요합니다. BUSINESS_NOT 역할을 가진 멤버를 승인합니다."
    )
    @ApiResponses(
            value = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "비즈니스 멤버 승인 성공",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한이 없는 사용자: 관리자 권한이 필요합니다.",
                            content = @Content(mediaType = "application/json")
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "멤버가 존재하지 않음",
                            content = @Content(mediaType = "application/json")
                    )
            }
    )
    @PostMapping("/members/{memberId}/approve")
    public ResponseEntity<ResponseDTO<String>> approveBusinessMember(@PathVariable Long memberId) {
        Long adminId = SecurityUtils.getCurrentMemberId();
        businessMemberService.approveBusinessMember(memberId, adminId);
        return ResponseEntity.ok(new ResponseDTO<>(SuccessCode.SUCCESS_APPROVE_BUSINESS_MEMBER, "비즈니스 멤버가 승인되었습니다."));
    }
}
