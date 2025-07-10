package com.ceos.beatbuddy.domain.coupon.repository;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberAndCouponAndCreatedAt(Member member, Coupon coupon, LocalDateTime receivedDate);

    boolean existsByMemberAndCoupon(Member member, Coupon coupon);
    int countByMemberAndCouponAndCreatedAtBetween(Member member, Coupon coupon, LocalDateTime startDate, LocalDateTime endDate);

    Page<MemberCoupon> findByMember_Id(Long memberId, Pageable pageable);

    Page<MemberCoupon> findByMember_IdAndStatusAndCoupon_ExpireDateAfter(
            Long memberId, MemberCoupon.CouponStatus status, Pageable pageable, LocalDate now
    );

    @Query("""
        SELECT mc FROM MemberCoupon mc
        WHERE mc.member.id = :memberId
        AND (
            mc.status = :status
            OR mc.coupon.expireDate < :now
        )
    """)
    Page<MemberCoupon> findUnavailableCoupons(
            @Param("memberId") Long memberId,
            @Param("status") MemberCoupon.CouponStatus status,
            @Param("now") LocalDate now,
            Pageable pageable
    );
}
