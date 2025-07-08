package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.redis.CouponQuotaRedisService;
import com.ceos.beatbuddy.domain.coupon.repository.CouponRepository;
import com.ceos.beatbuddy.domain.venue.dto.VenueCouponResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenueCouponService {
    private final CouponRepository couponRepository;
    private final CouponQuotaRedisService couponQuotaRedisService;

    public List<VenueCouponResponseDTO> getCouponsByVenue(Long venueId) {
        LocalDate today = LocalDate.now();

        // 1. venueId로 오늘 기준 만료되지 않은 쿠폰 조회
        List<Coupon> coupons = couponRepository.findAllValidByVenueId(venueId, today);

        // 2. Redis에서 잔여 수량 조회 후 DTO 변환
        return coupons.stream()
                .map(coupon -> {
                    int remaining = couponQuotaRedisService.getQuota(coupon.getId(), venueId, today);
                    return VenueCouponResponseDTO.toDTO(coupon, remaining);
                })
                .toList();
    }
}
