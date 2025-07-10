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
    private LocalDate expireDate;
    private int quota;          // 총 수량
    private int remaining;      // 남은 수량
    private boolean isSoldOut;  // 마감 여부
    private String policy;
    private String couponName; // 쿠폰 제목
    private String couponDescription; // 쿠폰 설명
    private String howToUse; // 사용 방법
    private String notes; // 유의사항
    // 내가 발급 받았는지 여부
    private boolean isReceived;
    private int maxQuota; // 최대 발급 가능 수량
    // 정책대로 판단
    private int receivedCount; // 내가 받은 쿠폰 개수

public static VenueCouponResponseDTO toDTO(Coupon coupon, int remaining, boolean isReceived, int receivedCount) {
        return VenueCouponResponseDTO.builder()
                .couponId(coupon.getId())
                .expireDate(coupon.getExpireDate())
                .quota(coupon.getQuota())
                .couponName(coupon.getName())
                .couponDescription(coupon.getContent())
                .howToUse(coupon.getHowToUse())
                .notes(coupon.getNotes())
                .remaining(remaining)
                .isSoldOut(remaining <= 0)
                .policy(coupon.getPolicy().name())
                .isReceived(isReceived)
                .maxQuota(coupon.getMaxReceiveCountPerUser())
                .receivedCount(receivedCount)
                .build();
    }
}