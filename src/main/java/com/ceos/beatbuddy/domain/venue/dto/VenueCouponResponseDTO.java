package com.ceos.beatbuddy.domain.venue.dto;

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
public class VenueCouponResponseDTO {

    private Long couponId;
    private String name;
    private LocalDate expireDate;
    private int quota;          // 총 수량
    private int remaining;      // 남은 수량
    private boolean isSoldOut;  // 마감 여부
    private String policy;
    private String howToUse; // 사용 방법
    private String notes; // 유의사항

    public static VenueCouponResponseDTO toDTO(Coupon coupon, int remaining) {
        return VenueCouponResponseDTO.builder()
                .couponId(coupon.getId())
                .name(coupon.getName())
                .expireDate(coupon.getExpireDate())
                .quota(coupon.getQuota())
                .howToUse(coupon.getHowToUse())
                .notes(coupon.getNotes())
                .remaining(remaining)
                .isSoldOut(remaining <= 0)
                .policy(coupon.getPolicy().name())
                .build();
    }
}