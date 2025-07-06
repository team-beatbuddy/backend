package com.ceos.beatbuddy.domain.coupon.application;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.coupon.dto.CouponCreateRequestDTO;
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
import org.springframework.data.redis.core.StringRedisTemplate;
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
    private final StringRedisTemplate redisTemplate;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public CouponReceiveResponseDTO receiveCoupon(Long couponId, Long memberId) {
        String redisKey = CouponRedisKeyUtil.getQuotaKey(couponId, LocalDate.now());

        Member member = memberService.validateAndGetMember(memberId);

        Coupon coupon = validateAndGetCoupon(couponId);

        if (coupon.getExpireDate().isBefore(LocalDate.now())) {
            throw new CustomException(CouponErrorCode.COUPON_EXPIRED);
        }

        if (coupon.getActive() != null && !coupon.getActive()) {
            throw new CustomException(CouponErrorCode.COUPON_DISABLED);
        }

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
        MemberCoupon memberCoupon = MemberCoupon.toEntity(member, coupon);
        memberCouponRepository.save(memberCoupon);

        CouponLuaScriptService.LuaResult result = luaScriptService.decreaseQuota(redisKey);

        if (result == CouponLuaScriptService.LuaResult.SOLD_OUT) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_SOLD_OUT);
        }

        if (result == CouponLuaScriptService.LuaResult.NOT_INITIALIZED) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_NOT_INITIALIZED);
        }

        return CouponReceiveResponseDTO.toDTO(memberCoupon.getId(), coupon, memberCoupon.getReceivedDate());
    }

    @Transactional
    public void createCoupon(CouponCreateRequestDTO request) {
        // 쿠폰 유효성 검사
        if (request.getQuota() <= 0) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_NOT_INITIALIZED);
        }

        if (request.getExpireDate().isBefore(LocalDate.now())) {
            throw new CustomException(CouponErrorCode.COUPON_EXPIRED);
        }

        // 1. 업장 유효성 검사
        Venue venue = null;
        if (request.getVenueId() != null) {
            venue = venueInfoService.validateAndGetVenue(request.getVenueId());
        }

        // 쿠폰 엔티티 생성
        Coupon coupon = CouponCreateRequestDTO.toEntity(request, venue);
        couponRepository.save(coupon);

        // 4. Redis quota 설정
        String redisKey = CouponRedisKeyUtil.getQuotaKey(coupon.getId(), LocalDate.now());

        redisTemplate.opsForValue().set(redisKey, String.valueOf(request.getQuota()));

    }

    @Transactional
    public void useCoupon(Long receiveCouponId, Long memberId) {
        // Member, Coupon 조회
        memberService.validateAndGetMember(memberId);

        // 쿠폰 유효성 검사 및 조회
        MemberCoupon memberCoupon = validateAndGetMemberCoupon(receiveCouponId);

        Coupon coupon = validateAndGetCoupon(memberCoupon.getCoupon().getId());

        if (coupon.getExpireDate().isBefore(LocalDate.now())) {
            throw new CustomException(CouponErrorCode.COUPON_EXPIRED);
        }

        // 이미 사용했는지 확인
        if (memberCoupon.getStatus() == MemberCoupon.CouponStatus.USED) {
            throw new CustomException(CouponErrorCode.COUPON_ALREADY_USED);
        }

        // 상태 변경
        memberCoupon.markUsed(); // status = USED
    }


    public Coupon validateAndGetCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(CouponErrorCode.COUPON_NOT_FOUND));
    }

    public MemberCoupon validateAndGetMemberCoupon(Long memberCouponId) {
        return memberCouponRepository.findById(memberCouponId)
                .orElseThrow(() -> new CustomException(CouponErrorCode.MEMBER_COUPON_NOT_FOUND));
    }
}