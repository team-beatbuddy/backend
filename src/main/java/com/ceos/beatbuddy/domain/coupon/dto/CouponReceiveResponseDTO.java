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
    private Long couponId;
    private String message;
    private LocalDate receivedDate;
    private LocalDateTime expireDate;

    public static CouponReceiveResponseDTO toDTO(Long couponId, Coupon coupon, LocalDate receivedDate) {
        return CouponReceiveResponseDTO.builder()
                .couponId(couponId)
                .receivedDate(receivedDate)
                .expireDate(coupon.getExpireDate())
                .build();
    }
}
