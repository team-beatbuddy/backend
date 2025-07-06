package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.application.CouponService;
import com.ceos.beatbuddy.domain.coupon.dto.CouponReceiveResponseDTO;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController {
    private final CouponService couponService;

    @PostMapping("/{couponId}/receive")
    public ResponseEntity<ResponseDTO<CouponReceiveResponseDTO>> receiveCoupon(
            @PathVariable Long couponId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        CouponReceiveResponseDTO result = couponService.receiveCoupon(couponId, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_RECEIVE_COUPON.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_RECEIVE_COUPON, result));
    }
}
