package com.ceos.beatbuddy.domain.coupon.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CouponRedisKeyUtil {

    public static String getQuotaKey(Long couponId, Long venueId, LocalDate date) {
        return String.format("venue_coupon_quota:%d:%d:%s",
                couponId, venueId, date.format(DateTimeFormatter.BASIC_ISO_DATE));
    }
}

