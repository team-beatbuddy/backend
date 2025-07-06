package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CouponCreateRequestDTO {
    private String name;
    private String howToUse;
    private String notes;
    private Long venueId;
    private LocalDate expireDate;
    private String policy;
    private int quota;

    public static Coupon toEntity(CouponCreateRequestDTO requestDTO, Venue venue) {
        return Coupon.builder()
                .name(requestDTO.getName())
                .howToUse(requestDTO.howToUse)
                .notes(requestDTO.getNotes())
                .expireDate(requestDTO.getExpireDate())
                .policy(Coupon.to(requestDTO.getPolicy()))
                .quota(requestDTO.getQuota())
                .active(true)
                .venue(venue)
                .build();
    }
}