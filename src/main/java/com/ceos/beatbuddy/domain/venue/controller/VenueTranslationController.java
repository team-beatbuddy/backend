package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Venue Translation", description = "베뉴 번역 관리 API")
@RestController
@RequestMapping("/api/admin/venues/translation")
@RequiredArgsConstructor
@Slf4j
public class VenueTranslationController {

    private final VenueInfoService venueInfoService;

    @Operation(
        summary = "전체 베뉴 번역 동기화",
        description = "번역이 누락된 모든 베뉴에 대해 Papago API를 사용하여 번역을 수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "번역 동기화 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/sync-all")
    public ResponseEntity<Map<String, String>> syncAllVenueTranslations() {
        log.info("전체 베뉴 번역 동기화 요청 수신");
        
        try {
            venueInfoService.syncVenueTranslations();
            return ResponseEntity.ok(Map.of(
                "message", "전체 베뉴 번역 동기화가 완료되었습니다.",
                "status", "success"
            ));
        } catch (Exception e) {
            log.error("전체 베뉴 번역 동기화 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "번역 동기화 중 오류가 발생했습니다: " + e.getMessage(),
                "status", "error"
            ));
        }
    }

    @Operation(
        summary = "특정 베뉴 번역 동기화",
        description = "지정된 베뉴 ID에 대해서만 번역을 수행합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "번역 동기화 성공"),
        @ApiResponse(responseCode = "404", description = "베뉴를 찾을 수 없음"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @PostMapping("/sync/{venueId}")
    public ResponseEntity<Map<String, String>> syncVenueTranslation(
        @Parameter(description = "번역할 베뉴 ID", required = true)
        @PathVariable Long venueId
    ) {
        log.info("베뉴 ID {} 번역 동기화 요청 수신", venueId);
        
        try {
            venueInfoService.syncVenueTranslation(venueId);
            return ResponseEntity.ok(Map.of(
                "message", String.format("베뉴 ID %d 번역 동기화가 완료되었습니다.", venueId),
                "status", "success",
                "venueId", venueId.toString()
            ));
        } catch (Exception e) {
            log.error("베뉴 ID {} 번역 동기화 중 오류 발생", venueId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "번역 동기화 중 오류가 발생했습니다: " + e.getMessage(),
                "status", "error",
                "venueId", venueId.toString()
            ));
        }
    }

    @Operation(
        summary = "번역 동기화 상태 조회",
        description = "전체 베뉴 중 번역이 완료된 비율과 번역이 필요한 베뉴 목록을 조회합니다."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "상태 조회 성공"),
        @ApiResponse(responseCode = "500", description = "서버 오류")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getTranslationSyncStatus() {
        log.info("번역 동기화 상태 조회 요청 수신");
        
        try {
            Map<String, Object> status = venueInfoService.getTranslationSyncStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("번역 동기화 상태 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "message", "상태 조회 중 오류가 발생했습니다: " + e.getMessage(),
                "status", "error"
            ));
        }
    }
}