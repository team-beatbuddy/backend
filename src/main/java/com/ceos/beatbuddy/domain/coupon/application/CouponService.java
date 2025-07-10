package com.ceos.beatbuddy.domain.coupon.application;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.coupon.dto.*;
import com.ceos.beatbuddy.domain.coupon.exception.CouponErrorCode;
import com.ceos.beatbuddy.domain.coupon.redis.CouponLuaScriptService;
import com.ceos.beatbuddy.domain.coupon.redis.CouponQuotaRedisService;
import com.ceos.beatbuddy.domain.coupon.redis.CouponRedisKeyUtil;
import com.ceos.beatbuddy.domain.coupon.repository.CouponRepository;
import com.ceos.beatbuddy.domain.coupon.repository.MemberCouponRepository;
import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.application.VenueInfoService;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import com.ceos.beatbuddy.global.code.SuccessCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponLuaScriptService luaScriptService;
    private final MemberService memberService;
    private final VenueInfoService venueInfoService;
    private final CouponRepository couponRepository;
    private final CouponQuotaRedisService couponQuotaRedisService;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public CouponReceiveResponseDTO receiveCoupon(Long venueId, Long couponId, Long memberId) {
        String redisKey = CouponRedisKeyUtil.getQuotaKey(venueId, couponId, LocalDate.now());

        Member member = memberService.validateAndGetMember(memberId);
        Coupon coupon = validateAndGetCoupon(couponId);
        Venue venue = venueInfoService.validateAndGetVenue(venueId);

        validateCouponAvailable(coupon);
        validateCouponReceivePolicy(member, coupon);

        // redis 에서 티켓 감소
        luaScriptService.decreaseQuotaOrThrow(redisKey);

        // DB 저장
        MemberCoupon memberCoupon = MemberCoupon.toEntity(venue, member, coupon);
        memberCouponRepository.save(memberCoupon);

        return CouponReceiveResponseDTO.toDTO(memberCoupon.getId(), coupon);
    }

    @Transactional
    public void createCoupon(CouponCreateRequestDTO request) {
        validateCreateCouponAvailable(request.getQuota(), request.getExpireDate());

        List<Venue> venues = venueInfoService.validateAndGetVenues(request.getVenueIds());

        Coupon coupon = CouponCreateRequestDTO.toEntity(request, venues);
        couponRepository.save(coupon);

        List<Long> venueIds = venues.stream().map(Venue::getId).toList();
        couponQuotaRedisService.setQuota(coupon.getId(), venueIds, request.getQuota(), coupon.getExpireDate());
    }

    @Transactional
    public void updateCoupon(Long couponId, CouponUpdateRequestDTO dto) {
        Coupon coupon = validateAndGetCoupon(couponId);
        List<Venue> venues = venueInfoService.validateAndGetVenues(dto.getVenueIds());

        // 이전 만료일 저장
        LocalDate previousExpireDate = coupon.getExpireDate();

        // 쿠폰 업데이트
        coupon.updateFromRequest(dto, venues);

        // 만료일 변경되었는지 체크 후 TTL 재설정
        if (!previousExpireDate.isEqual(coupon.getExpireDate())) {
            List<Long> venueIds = venues.stream()
                    .map(Venue::getId)
                    .toList();
            couponQuotaRedisService.updateQuotaTTL(coupon.getId(), venueIds, coupon.getExpireDate());
        }
    }

    @Transactional
    public void useCoupon(Long receiveCouponId, Long memberId) {
        // Member, Coupon 조회
        memberService.validateAndGetMember(memberId);

        // 쿠폰 유효성 검사 및 조회
        MemberCoupon memberCoupon = validateAndGetMemberCoupon(receiveCouponId);

        Coupon coupon = validateAndGetCoupon(memberCoupon.getCoupon().getId());

        // 중복된 만료 검사 대신 재사용 가능한 공통 메서드 호출
        validateCouponAvailable(coupon);

        // 이미 사용했는지 확인
        if (memberCoupon.getStatus() == MemberCoupon.CouponStatus.USED) {
            throw new CustomException(CouponErrorCode.COUPON_ALREADY_USED);
        }

        // 상태 변경
        memberCoupon.markUsed(); // status = USED
        memberCoupon.setUsedDate(LocalDateTime.now());
    }

    public MyPageCouponList getMyAllCouponAvailable(Long memberId, int page, int size) {
         memberService.validateAndGetMember(memberId);

        // 페이지네이션 잘못됐는지 확인
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        // 멤버의 쿠폰 정보 조회 (페이지네이션으로 조회)
        Page<MemberCoupon> memberCouponsPage = memberCouponRepository.findByMember_IdAndStatusAndCoupon_ExpireDateAfter
                (memberId, MemberCoupon.CouponStatus.RECEIVED, PageRequest.of(page - 1, size), LocalDate.now());

        // 쿠폰 정보와 함께 반환
        return convertToMyPageCouponList(memberCouponsPage);
    }

    public MyPageCouponList getMyAllCouponUnavailable(Long memberId, int page, int size) {
        memberService.validateAndGetMember(memberId);

        // 페이지네이션 잘못됐는지 확인
        if (page < 1) {
            throw new CustomException(ErrorCode.PAGE_OUT_OF_BOUNDS);
        }

        // 멤버의 쿠폰 정보 조회 (페이지네이션으로 조회)
        Page<MemberCoupon> memberCouponsPage = memberCouponRepository.findUnavailableCoupons
                (memberId, MemberCoupon.CouponStatus.USED, LocalDate.now(), PageRequest.of(page - 1, size));

        // 쿠폰 정보와 함께 반환
        return convertToMyPageCouponList(memberCouponsPage);
    }

    private MyPageCouponList convertToMyPageCouponList(Page<MemberCoupon> memberCouponsPage) {
        // 쿠폰 정보와 함께 반환
        List<MyPageCouponDTO> myPageCoupons = memberCouponsPage.stream()
                .map(MyPageCouponDTO::toDTO)
                .toList();

        // 페이지 정보와 함께 반환
        if (myPageCoupons.isEmpty()) {
            throw new CustomException(SuccessCode.SUCCESS_BUT_EMPTY_LIST);
        }

        // 페이지 정보 설정
        int totalPages = memberCouponsPage.getTotalPages();
        int totalElements = (int) memberCouponsPage.getTotalElements();
        int currentPage = memberCouponsPage.getNumber() + 1; // 페이지는 0부터 시작하므로 +1
        int sizePerPage = memberCouponsPage.getSize();

        return MyPageCouponList.builder()
                .totalCount(totalElements)
                .totalPage(totalPages)
                .currentPage(currentPage)
                .pageSize(sizePerPage)
                .coupons(myPageCoupons)
                .build();
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
        LocalDateTime today = LocalDateTime.now();

        switch (coupon.getPolicy()) {
            case DAILY -> {
                if (memberCouponRepository.existsByMemberAndCouponAndCreatedAt(member, coupon, today)) {
                    throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED_TODAY);
                }
            }
            case ONCE -> {
                if (memberCouponRepository.existsByMemberAndCoupon(member, coupon)) {
                    throw new CustomException(CouponErrorCode.COUPON_ALREADY_RECEIVED);
                }
            }
            case WEEKLY -> {
                if (coupon.getMaxReceiveCountPerUser() != null) {
                    // 주 시작(월요일)과 종료(일요일) 계산
                    LocalDateTime startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
                    LocalDateTime endOfWeek = today.with(java.time.DayOfWeek.SUNDAY);

                    int countThisWeek = memberCouponRepository.countByMemberAndCouponAndCreatedAtBetween(
                            member, coupon, startOfWeek, endOfWeek);

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