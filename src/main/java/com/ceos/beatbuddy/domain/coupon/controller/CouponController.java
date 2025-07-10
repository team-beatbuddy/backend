package com.ceos.beatbuddy.domain.coupon.controller;

import com.ceos.beatbuddy.domain.coupon.application.CouponService;
import com.ceos.beatbuddy.domain.coupon.dto.CouponReceiveResponseDTO;
import com.ceos.beatbuddy.domain.coupon.dto.MyPageCouponList;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.dto.ResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/coupons")
public class CouponController implements CouponApiDocs {
    private final CouponService couponService;

    @Override
    @PostMapping("/{venueId}/{couponId}/receive")
    public ResponseEntity<ResponseDTO<CouponReceiveResponseDTO>> receiveCoupon(
            @PathVariable Long venueId,
            @PathVariable Long couponId) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        CouponReceiveResponseDTO result = couponService.receiveCoupon(venueId, couponId, memberId);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_RECEIVE_COUPON.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_RECEIVE_COUPON, result));
    }

    // 사용 가능
    @Override
    @GetMapping("/my-coupons/available")
    public ResponseEntity<ResponseDTO<MyPageCouponList>> getMyAvailableCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MyPageCouponList myPageCouponList = couponService.getMyAllCouponAvailable(memberId, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MEMBER_COUPON_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MEMBER_COUPON_LIST, myPageCouponList));
    }

    @Override
    @GetMapping("/my-coupons/unavailable")
    public ResponseEntity<ResponseDTO<MyPageCouponList>> getMyUnavailableCoupons(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        MyPageCouponList myPageCouponList = couponService.getMyAllCouponUnavailable(memberId, page, size);
        return ResponseEntity
                .status(SuccessCode.SUCCESS_GET_MEMBER_COUPON_LIST.getStatus().value())
                .body(new ResponseDTO<>(SuccessCode.SUCCESS_GET_MEMBER_COUPON_LIST, myPageCouponList));
    }

}
