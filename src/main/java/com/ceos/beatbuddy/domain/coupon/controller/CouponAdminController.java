package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.application.CouponService;
import com.ceos.beatbuddy.domain.coupon.dto.CouponCreateRequestDTO;
import com.ceos.beatbuddy.domain.coupon.dto.CouponUpdateRequestDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/coupons")
public class CouponAdminController implements CouponAdminApiDocs {
    private final CouponService couponService;

    @Override
    @PostMapping("")
    public ResponseEntity<ResponseDTO<String>> createCoupon(@Valid @RequestBody CouponCreateRequestDTO request) {
        couponService.createCoupon(request);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_CREATE_COUPON.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_CREATE_COUPON, "쿠폰 등록 성공"));
    }

    @Override
    @PostMapping("/{receiveCouponId}/use")
    public ResponseEntity<ResponseDTO<String>> useCoupon(
            @PathVariable Long receiveCouponId
    ) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        couponService.useCoupon(receiveCouponId, memberId);

        return ResponseEntity
                .status(SuccessCode.SUCCESS_USE_COUPON.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_USE_COUPON, "쿠폰 사용 성공"));
    }

    @PatchMapping("/{couponId}")
    public ResponseEntity<ResponseDTO<String>> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody CouponUpdateRequestDTO request) {
        couponService.updateCoupon(couponId, request);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_UPDATE.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_UPDATE, "쿠폰 수정 성공"));
    }
}
