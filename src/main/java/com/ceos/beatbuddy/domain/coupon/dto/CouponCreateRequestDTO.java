package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
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
public class CouponCreateRequestDTO {
    private String name;
    private String howToUse;
    private String notes;
    private List<Long> venueIds;
    private LocalDate expireDate;
    private String policy;
    private int quota;
    private Integer maxReceiveCountPerUser;
    private Integer sameVenueUse;

    public static Coupon toEntity(CouponCreateRequestDTO requestDTO, List<Venue> venues) {
        return Coupon.builder()
                .name(requestDTO.getName())
                .howToUse(requestDTO.getHowToUse())
                .notes(requestDTO.getNotes())
                .expireDate(requestDTO.getExpireDate())
                .policy(Coupon.to(requestDTO.getPolicy()))
                .quota(requestDTO.getQuota())
                .active(true)
                .venues(venues)
                .maxReceiveCountPerUser(requestDTO.getMaxReceiveCountPerUser())
                .sameVenueUse(requestDTO.getSameVenueUse())
                .build();
    }
}