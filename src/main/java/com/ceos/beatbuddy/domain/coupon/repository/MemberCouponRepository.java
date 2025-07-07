package com.ceos.beatbuddy.domain.coupon.repository;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.domain.MemberCoupon;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.venue.entity.Venue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    boolean existsByMemberAndCouponAndReceivedDate(Member member, Coupon coupon, LocalDate receivedDate);

    boolean existsByMemberAndCoupon(Member member, Coupon coupon);

    Optional<MemberCoupon> findByMemberAndCouponAndReceivedDate(Member member, Coupon coupon, LocalDate date);

    @Query("""
        SELECT COUNT(mc)
        FROM MemberCoupon mc
        WHERE mc.member = :member AND mc.coupon = :coupon
          AND FUNCTION('YEARWEEK', mc.receivedDate) = FUNCTION('YEARWEEK', :date)
        """)
    int countByMemberAndCouponAndWeek(Member member, Coupon coupon, LocalDate date);

    // venue 주간 사용 횟수
    @Query("""
        SELECT COUNT(mc)
        FROM MemberCoupon mc
        WHERE mc.member = :member
          AND mc.venue = :venue
          AND mc.status = 'USED'
          AND FUNCTION('YEARWEEK', mc.receivedDate) = FUNCTION('YEARWEEK', :date)
    """)
    int countUsedCouponsByMemberAndVenueThisWeek(Member member, Venue venue, LocalDate date);

    @Query("""
    SELECT COUNT(mc)
    FROM MemberCoupon mc
    WHERE mc.member = :member
      AND mc.venue = :venue
      AND mc.coupon = :coupon
      AND FUNCTION('YEARWEEK', mc.receivedDate) = FUNCTION('YEARWEEK', :date)
""")
    int countByMemberAndVenueAndCouponAndWeek(Member member, Venue venue, Coupon coupon, LocalDate date);

}
