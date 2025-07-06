package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponReceiveResponseDTO {
    private Long receivedCouponId;
    private LocalDate receivedDate;
    private LocalDate expireDate;

    public static CouponReceiveResponseDTO toDTO(Long receivedCouponId, Coupon coupon, LocalDate receivedDate) {
        return CouponReceiveResponseDTO.builder()
                .receivedCouponId(receivedCouponId)
                .receivedDate(receivedDate)
                .expireDate(coupon.getExpireDate())
                .build();
    }
}
