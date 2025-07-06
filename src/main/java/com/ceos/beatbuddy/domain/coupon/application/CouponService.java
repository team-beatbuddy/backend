package com.ceos.beatbuddy.domain.coupon.application;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.coupon.dto.CouponReceiveResponseDTO;
import com.ceos.beatbuddy.domain.coupon.exception.CouponErrorCode;
import com.ceos.beatbuddy.domain.coupon.redis.CouponLuaScriptService;
import com.ceos.beatbuddy.domain.coupon.redis.CouponRedisKeyUtil;
import com.ceos.beatbuddy.domain.coupon.repository.CouponRepository;
import com.ceos.beatbuddy.domain.coupon.repository.MemberCouponRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponLuaScriptService luaScriptService;
    private final MemberService memberService;
    private final VenueInfoService venueInfoService;
    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public CouponReceiveResponseDTO receiveCoupon(Long couponId, Long venueId, Long memberId) {
        String redisKey = CouponRedisKeyUtil.getQuotaKey(venueId, couponId, LocalDate.now());

        CouponLuaScriptService.LuaResult result = luaScriptService.decreaseQuota(redisKey);

        if (result == CouponLuaScriptService.LuaResult.SOLD_OUT) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_SOLD_OUT);
        }

        if (result == CouponLuaScriptService.LuaResult.NOT_INITIALIZED) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_NOT_INITIALIZED);
        }

        Member member = memberService.validateAndGetMember(memberId);

        Coupon coupon = validateAndGetCoupon(couponId);

        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        // 중복 수령 방지 로직
        boolean alreadyReceived;

        if (coupon.getPolicy() == Coupon.CouponPolicy.DAILY) {
            alreadyReceived = memberCouponRepository.existsByMemberAndCouponAndReceivedDate(member, coupon, LocalDate.now());
            if (alreadyReceived) {
                throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED);
            }
        } else if (coupon.getPolicy() == Coupon.CouponPolicy.ONCE) {
            alreadyReceived = memberCouponRepository.existsByMemberAndCoupon(member, coupon);
            if (alreadyReceived) {
                throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED_TODAY);
            }
        }

        // DB 저장
        MemberCoupon memberCoupon = MemberCoupon.toEntity(member, coupon, venue);
        memberCouponRepository.save(memberCoupon);

        return CouponReceiveResponseDTO.toDTO(couponId, coupon, memberCoupon.getReceivedDate());
    }

    public Coupon validateAndGetCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(CouponErrorCode.COUPON_NOT_FOUND));
    }
}