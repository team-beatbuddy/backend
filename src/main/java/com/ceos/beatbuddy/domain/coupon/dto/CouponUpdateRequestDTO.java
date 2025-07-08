package com.ceos.beatbuddy.domain.coupon.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponUpdateRequestDTO {
    private String name;
    private String howToUse;
    private String notes;
    private List<Long> venueIds;
    private LocalDate expireDate;
    private String policy;
    private Integer quota;
    private Integer maxReceiveCountPerUser;
    private Integer sameVenueUse;
}