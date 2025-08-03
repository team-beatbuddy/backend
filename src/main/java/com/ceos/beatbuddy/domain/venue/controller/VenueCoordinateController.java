package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.application.VenueCoordinateUpdateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/venues/coordinates")
@RequiredArgsConstructor
@Tag(name = "베뉴 좌표 관리", description = "베뉴 위도/경도 좌표 업데이트 API")
public class VenueCoordinateController {

    private final VenueCoordinateUpdateService venueCoordinateUpdateService;

    @PostMapping("/update/all")
    @Operation(summary = "모든 베뉴 좌표 업데이트", 
               description = "데이터베이스의 모든 베뉴에 대해 주소를 기반으로 위도/경도를 업데이트합니다. 비동기로 처리됩니다.")
    public ResponseEntity<String> updateAllVenueCoordinates() {
        CompletableFuture<Void> future = venueCoordinateUpdateService.updateAllVenueCoordinates();
        
        return ResponseEntity.ok("베뉴 좌표 업데이트 작업이 시작되었습니다. 로그를 확인해주세요.");
    }

    @PostMapping("/update/{venueId}")
    @Operation(summary = "특정 베뉴 좌표 업데이트", 
               description = "지정된 베뉴의 주소를 기반으로 위도/경도를 업데이트합니다.")
    public ResponseEntity<String> updateSpecificVenueCoordinate(@PathVariable Long venueId) {
        CompletableFuture<Void> future = venueCoordinateUpdateService.updateSpecificVenueCoordinate(venueId);
        
        return ResponseEntity.ok("베뉴 ID " + venueId + "의 좌표 업데이트 작업이 시작되었습니다.");
    }

    @GetMapping("/check")
    @Operation(summary = "좌표 없는 베뉴 확인", 
               description = "좌표가 설정되지 않은 베뉴들의 목록을 로그로 출력합니다.")
    public ResponseEntity<String> checkVenuesWithoutCoordinates() {
        venueCoordinateUpdateService.printVenuesWithoutCoordinates();
        
        return ResponseEntity.ok("좌표가 없는 베뉴 목록을 로그로 출력했습니다.");
    }
}