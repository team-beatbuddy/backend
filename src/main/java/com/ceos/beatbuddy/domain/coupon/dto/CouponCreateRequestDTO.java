package com.ceos.beatbuddy.domain.coupon.dto;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "쿠폰명은 필수입니다")
    private String name;
    @NotNull(message = "쿠폰 내용은 필수입니다")
    private String content; // 쿠폰 내용 (팝업 제목)
    @NotNull(message = "사용방법은 필수입니다")
    private String howToUse;
    private String notes;
    @NotNull(message = "적용 업장은 최소 1개 이상이어야 합니다")
    private List<Long> venueIds;
    @Future(message = "만료일은 현재 날짜보다 미래여야 합니다")
    private LocalDate expireDate;
    @NotNull(message = "쿠폰 정책은 필수입니다. (예: ONCE, DAILY, WEEKLY)")
    private String policy;
    @Min(value = 1, message = "쿠폰 수량은 1개 이상이어야 합니다")
    private int quota;
    @Min(value = 1, message = "사용자당 최대 수령 횟수는 1회 이상이어야 합니다")
    private Integer maxReceiveCountPerUser;
    @Min(value = 1, message = "같은 업장 사용 횟수는 1회 이상이어야 합니다")
    private Integer sameVenueUse;

    public static Coupon toEntity(CouponCreateRequestDTO requestDTO, List<Venue> venues) {
        return Coupon.builder()
                .name(requestDTO.getName())
                .content(requestDTO.getContent())
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