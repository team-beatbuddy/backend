package com.ceos.beatbuddy.domain.coupon.repository;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberAndCouponAndCreatedAt(Member member, Coupon coupon, LocalDateTime receivedDate);

    boolean existsByMemberAndCoupon(Member member, Coupon coupon);
    int countByMemberAndCouponAndCreatedAtBetween(Member member, Coupon coupon, LocalDateTime startDate, LocalDateTime endDate);

    Page<MemberCoupon> findByMember_Id(Long memberId, Pageable pageable);

    Page<MemberCoupon> findByMember_IdAndStatusAndCoupon_ExpireDateAfter(
            Long memberId, MemberCoupon.CouponStatus status, Pageable pageable, LocalDateTime now
    );
}
