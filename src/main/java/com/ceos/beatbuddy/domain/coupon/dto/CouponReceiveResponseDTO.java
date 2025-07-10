package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponReceiveResponseDTO {
    private Long receivedCouponId;
    private LocalDateTime receivedDate;
    private LocalDate expireDate;

    public static CouponReceiveResponseDTO toDTO(Long receivedCouponId, Coupon coupon) {
        return CouponReceiveResponseDTO.builder()
                .receivedCouponId(receivedCouponId)
                .receivedDate(LocalDateTime.now())
                .expireDate(coupon.getExpireDate())
                .build();
    }
}
