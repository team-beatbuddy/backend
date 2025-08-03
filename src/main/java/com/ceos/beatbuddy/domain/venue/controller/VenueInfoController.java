package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.event.dto.EventListResponseDTO;
import com.ceos.beatbuddy.domain.venue.application.VenueCouponService;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.dto.VenueCouponResponseDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueInfoResponseDTO;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "VenueInfo Controller", description = "베뉴에 대한 정보를 제공하는 컨트롤러")
@RequestMapping("/venue-info")
@Validated
public class VenueInfoController implements VenueInfoApiDocs {
    private final VenueInfoService venueInfoService;
    private final VenueCouponService venueCouponService;

    @GetMapping
    @Operation(summary = "존재하는 모든 베뉴의 리스트 조회", description = "모든 베뉴의 목록을 조회합니다")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "모든 베뉴 목록 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Venue.class))),
            @ApiResponse(responseCode = "404", description = "조회할 베뉴가 없습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<List<Venue>> getAllVenueInfo() {
        return ResponseEntity.ok(venueInfoService.getVenueInfoList());
    }

    @GetMapping("/{venueId}")
    @Operation(summary = "베뉴 상세정보 조회", description = "베뉴에 대한 상세페이지 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "베뉴 정보 조회 성공",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VenueInfoResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 베뉴가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<VenueInfoResponseDTO> getVenueInfo(@PathVariable Long venueId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        return ResponseEntity.ok(venueInfoService.getVenueInfo(venueId, memberId));
    }

    @Override
    @GetMapping("/{venueId}/coupons")
    @Operation(summary = "베뉴 쿠폰 조회", description = "특정 베뉴의 쿠폰 목록을 조회합니다.")
    public ResponseEntity<ResponseDTO<List<VenueCouponResponseDTO>>> getCouponsByVenue(@PathVariable Long venueId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<VenueCouponResponseDTO> coupons = venueCouponService.getCouponsByVenue(venueId, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_VENUE_COUPONS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_VENUE_COUPONS, coupons));
    }

    @Override
    @GetMapping("/{venueId}/events/latest")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventsByVenueLatest(@PathVariable Long venueId,
                                                                                            @RequestParam(defaultValue = "1") int page,
                                                                                            @RequestParam(defaultValue = "10") int size,
                                                                                                  @RequestParam(defaultValue = "false") boolean isPast) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO eventListResponseDTO = venueInfoService.getVenueEventsLatest(venueId, memberId, page, size, isPast);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_VENUE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_VENUE_EVENTS, eventListResponseDTO));
    }


    @Override
    @GetMapping("/{venueId}/events/popular")
    public ResponseEntity<ResponseDTO<EventListResponseDTO>> getEventsByVenuePopular(@PathVariable Long venueId,
                                                                                                  @RequestParam(defaultValue = "1") int page,
                                                                                                  @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        EventListResponseDTO eventListResponseDTO = venueInfoService.getVenueEventsByPopularity(venueId, memberId, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_VENUE_EVENTS.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_VENUE_EVENTS, eventListResponseDTO));
    }
}
