package com.ceos.beatbuddy.domain.venue.application;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.redis.CouponQuotaRedisService;
import com.ceos.beatbuddy.domain.coupon.repository.CouponRepository;
import com.ceos.beatbuddy.domain.coupon.repository.MemberCouponRepository;
import com.ceos.beatbuddy.domain.venue.dto.VenueCouponResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.ceos.beatbuddy.domain.coupon.domain.Coupon.CouponPolicy.*;
import static com.ceos.beatbuddy.domain.coupon.domain.QCoupon.coupon;

@Service
@RequiredArgsConstructor
public class VenueCouponService {
    private final CouponRepository couponRepository;
    private final CouponQuotaRedisService couponQuotaRedisService;
    private final MemberCouponRepository memberCouponRepository;

    public List<VenueCouponResponseDTO> getCouponsByVenue(Long venueId, Long memberId) {
        LocalDate today = LocalDate.now();

        // 1. venueId로 오늘 기준 만료되지 않은 쿠폰 조회
        List<Coupon> coupons = couponRepository.findAllValidByVenueId(venueId, today);

        // 2. Redis에서 잔여 수량 조회 후 DTO 변환
        return coupons.stream()
                .map(coupon -> {
                    int remaining = couponQuotaRedisService.getQuota(coupon.getId(), venueId, today);
                    // 3. 쿠폰 정책에 따라 내가 받은 쿠폰 개수 조회
                    int receivedCount = getReceivedCount(memberId, coupon, venueId);
                    boolean isReceived = memberCouponRepository.existsByMemberIdAndCouponIdAndVenueId(
                            memberId, coupon.getId(), venueId
                    );
                    return VenueCouponResponseDTO.toDTO(coupon, remaining, isReceived, receivedCount);
                })
                .toList();
    }

    private int getReceivedCount(Long memberId, Coupon coupon, Long venueId) {
        return switch (coupon.getPolicy()) {
            case DAILY -> memberCouponRepository.countByMemberIdAndCouponIdAndVenueIdAndCreatedAt(
                    memberId, coupon.getId(), venueId, LocalDateTime.now()
            );
            case WEEKLY -> {
                LocalDate now = LocalDate.now();
                LocalDate startOfWeek = now.with(DayOfWeek.MONDAY);
                LocalDate endOfWeek = now.with(DayOfWeek.SUNDAY);
                yield memberCouponRepository.countByMemberIdAndCouponIdAndVenueIdAndCreatedAtBetween(
                        memberId, coupon.getId(), venueId, startOfWeek.atStartOfDay(), endOfWeek.atTime(23,59,59)
                );
            }
            case ONCE -> memberCouponRepository.countByMemberIdAndCouponIdAndVenueId(
                    memberId, coupon.getId(), venueId
            );
        };
    }
}
