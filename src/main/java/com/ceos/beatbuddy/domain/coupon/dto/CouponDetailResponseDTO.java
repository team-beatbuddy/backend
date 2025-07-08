package com.ceos.beatbuddy.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CouponDetailResponseDTO {
    private Long couponId;
    private String name;
    private String description;
    private String imageUrl;
    private int quota;
    private int usedQuota;
    private String startDate;
    private String endDate;
}
