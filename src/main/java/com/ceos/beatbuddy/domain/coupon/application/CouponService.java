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
import java.util.List;

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
    public CouponReceiveResponseDTO receiveCoupon(Long venueId, Long couponId, Long memberId) {
        String redisKey = CouponRedisKeyUtil.getQuotaKey(couponId, LocalDate.now());

        Member member = memberService.validateAndGetMember(memberId);
        Coupon coupon = validateAndGetCoupon(couponId);
        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        validateCouponAvailable(coupon);
        validateCouponReceivePolicy(member, coupon);

        // DB 저장
        MemberCoupon memberCoupon = MemberCoupon.toEntity(venue, member, coupon);
        memberCouponRepository.save(memberCoupon);

        // redis 에서 티켓 감소
        luaScriptService.decreaseQuotaOrThrow(redisKey);

        return CouponReceiveResponseDTO.toDTO(memberCoupon.getId(), coupon, memberCoupon.getReceivedDate());
    }

    @Transactional
    public void createCoupon(CouponCreateRequestDTO request) {
        // 쿠폰 유효성 검사
        validateCreateCouponAvailable(request.getQuota(), request.getExpireDate());

        // 1. 업장 유효성 검사
        List<Venue> venues = venueInfoService.validateAndGetVenues(request.getVenueIds());

        // 쿠폰 엔티티 생성
        Coupon coupon = CouponCreateRequestDTO.toEntity(request, venues);
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

    private void validateCouponReceivePolicy(Member member, Coupon coupon) {
        LocalDate today = LocalDate.now();

        switch (coupon.getPolicy()) {
            case DAILY -> {
                if (memberCouponRepository.existsByMemberAndCouponAndReceivedDate(member, coupon, today)) {
                    throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED);
                }
            }
            case ONCE -> {
                if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
                    throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED_TODAY);
                }
            }
            case WEEKLY -> {
                if (coupon.getMaxReceiveCountPerUser() != null) {
                    int countThisWeek = memberCouponRepository.countByMemberAndCouponAndWeek(member, coupon, today);
                    if (countThisWeek >= coupon.getMaxReceiveCountPerUser()) {
                        throw new CustomException(CouponErrorCode.COUPON_RECEIVE_LIMIT_EXCEEDED);
                    }
                }
            }
        }
    }
    private void validateCreateCouponAvailable(int quota, LocalDate expireDate) {
        if (quota <= 0) {
            throw new CustomException(CouponErrorCode.COUPON_QUOTA_NOT_INITIALIZED);
        }

        if (expireDate.isBefore(LocalDate.now())) {
            throw new CustomException(CouponErrorCode.COUPON_EXPIRED);
        }
    }
    private void validateCouponAvailable(Coupon coupon) {
        if (coupon.getExpireDate().isBefore(LocalDate.now())) {
            throw new CustomException(CouponErrorCode.COUPON_EXPIRED);
        }
        if (Boolean.FALSE.equals(coupon.getActive())) {
            throw new CustomException(CouponErrorCode.COUPON_DISABLED);
        }
    }
}