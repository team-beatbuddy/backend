package com.ceos.beatbuddy.domain.admin.controller;

import com.ceos.beatbuddy.domain.admin.dto.ReportSummaryDTO;
import com.ceos.beatbuddy.domain.coupon.application.CouponService;
import com.ceos.beatbuddy.domain.coupon.dto.CouponCreateRequestDTO;
import com.ceos.beatbuddy.domain.member.dto.AdminResponseDto;
import com.ceos.beatbuddy.domain.admin.application.AdminService;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.dto.LoginRequest;
import com.ceos.beatbuddy.domain.venue.dto.VenueRequestDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController implements AdminApiDocs {
    private final VenueInfoService venueInfoService;
    private final AdminService adminService;
    private final CouponService couponService;


    @Override
    @PostMapping
    public ResponseEntity<Long> PostVenueInfo(@RequestBody VenueRequestDTO venueRequestDTO,
                                              @Parameter(description = "로고 이미지", required = false,
                                                      content = @Content(mediaType = "multipart/form-data"))
                                              @RequestParam(value = "file", required = false) MultipartFile logoImage,
                                              @Parameter(description = "배경 이미지, 비디오 파일", required = false,
                                                      content = @Content(mediaType = "multipart/form-data"))
                                              @RequestParam(value = "file", required = false) List<MultipartFile> backgroundImage)
            throws IOException {
        return ResponseEntity.ok(
                venueInfoService.addVenueInfo(venueRequestDTO, logoImage, backgroundImage).getId());
    }


    @Override
    @DeleteMapping("/{venueId}")
    public ResponseEntity<Long> DeleteVenueInfo(@PathVariable Long venueId) {
        return ResponseEntity.ok(venueInfoService.deleteVenueInfo(venueId));
    }


    @Override
    @PutMapping("/{venueId}")
    public ResponseEntity<Long> updateVenueInfo(@PathVariable Long venueId,
                                                @RequestBody VenueRequestDTO venueRequestDTO,
                                                @Parameter(description = "로고 이미지", required = false,
                                                        content = @Content(mediaType = "multipart/form-data"))
                                                @RequestParam(value = "file", required = false) MultipartFile logoImage,
                                                @Parameter(description = "배경 이미지, 비디오 파일", required = false,
                                                        content = @Content(mediaType = "multipart/form-data"))
                                                @RequestParam(value = "file", required = false) List<MultipartFile> backgroundImage)
            throws IOException {
        return ResponseEntity.ok(
                venueInfoService.updateVenueInfo(venueId, venueRequestDTO, logoImage, backgroundImage).getId());
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

    @GetMapping("/report")
    public ResponseEntity<ResponseDTO<List<ReportSummaryDTO>>> getAllReports() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<ReportSummaryDTO> reports = adminService.getAllReports(memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_REPORT_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_REPORT_LIST, reports));
    }

}
