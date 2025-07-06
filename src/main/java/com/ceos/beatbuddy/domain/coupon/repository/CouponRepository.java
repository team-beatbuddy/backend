package com.ceos.beatbuddy.domain.coupon.repository;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    // Custom query methods can be defined here if needed
    // For example, to find coupons by venueId or other criteria
}
