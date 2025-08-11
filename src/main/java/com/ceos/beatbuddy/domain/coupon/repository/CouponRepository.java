package com.ceos.beatbuddy.domain.coupon.repository;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    @Query("""
    SELECT c FROM Coupon c
    JOIN c.venues v
    WHERE v.id = :venueId AND c.expireDate >= :today
""")
    List<Coupon> findAllValidByVenueId(@Param("venueId") Long venueId, @Param("today") LocalDate today);

    boolean existsByVenues_IdAndExpireDateIsAfter(Long venueId, LocalDate now);
}
