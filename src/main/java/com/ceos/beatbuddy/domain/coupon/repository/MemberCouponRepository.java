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
    int countByMemberAndCouponAndReceivedDateBetween(Member member, Coupon coupon, LocalDate startDate, LocalDate endDate);
}
