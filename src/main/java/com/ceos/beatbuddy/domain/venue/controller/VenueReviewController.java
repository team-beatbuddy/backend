package com.ceos.beatbuddy.domain.venue.controller;

import com.ceos.beatbuddy.domain.venue.application.VenueReviewService;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewRequestDTO;
import com.ceos.beatbuddy.domain.venue.dto.VenueReviewResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "Venue Review Controller", description = "베뉴 리뷰 컨트롤러\n"
        + "사용자가 베뉴에 대한 리뷰를 작성하고, 수정, 삭제 등을 할 수 있습니다.")
@RequestMapping("/venue-reviews")
public class VenueReviewController implements VenueReviewApiDocs {
    private final VenueReviewService venueReviewService; // 주석 처리된 부분은 실제 서비스 로직을 구현할 때 사용합니다.

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE,
            value = "/{venueId}")
         public ResponseEntity<ResponseDTO<VenueReviewResponseDTO>> createVenueReview(
                 @PathVariable Long venueId,
                 @Valid @RequestPart("venueReviewRequestDTO") VenueReviewRequestDTO venueReviewRequestDTO,
                 @RequestPart(value = "images", required = false) List<MultipartFile> images) {
             Long memberId = SecurityUtils.getCurrentMemberId();
             VenueReviewResponseDTO result = venueReviewService.createVenueReview(venueId, memberId, venueReviewRequestDTO, images);

             return ResponseEntity
                     .status(SuccessCode.SUCCESS_CREATE_VENUE_REVIEW.getStatus().value())
                     .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATE_VENUE_REVIEW, result));
         }


     @Override
    @GetMapping(value = "/{venueId}/{sort}")
    public ResponseEntity<ResponseDTO<List<VenueReviewResponseDTO>>> getReviewFilterImageOrNot(
            @PathVariable Long venueId,
            @PathVariable String sort,
            @RequestParam(name = "hasImage", required = false, defaultValue = "false") boolean hasImage) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        List<VenueReviewResponseDTO> result = venueReviewService.getVenueReview(venueId, memberId, hasImage, sort);

        if (result.isEmpty()) {
            return ResponseEntity
                    .status(SuccessCode.SUCCESS_GET_VENUE_REVIEW.getStatus().value())
                    .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_VENUE_REVIEW, List.of()));
        }

         // 리뷰가 존재하는 경우

        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_VENUE_REVIEW.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_VENUE_REVIEW, result));
    }

    @Override
    @PostMapping("/{venueReviewId}/like")
    public ResponseEntity<ResponseDTO<String>> likeVenueReview(
            @PathVariable Long venueReviewId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        venueReviewService.likeVenueReview(venueReviewId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_LIKE_VENUE_REVIEW.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_LIKE_VENUE_REVIEW, "좋아요를 눌렀습니다."));
    }


}
