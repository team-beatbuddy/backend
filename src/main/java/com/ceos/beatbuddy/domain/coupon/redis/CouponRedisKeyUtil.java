package com.ceos.beatbuddy.domain.coupon.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CouponRedisKeyUtil {

    public static String getQuotaKey(Long venueId, Long couponId, LocalDate date) {
        return String.format("venue_coupon_quota:%d:%d:%s",
                venueId, couponId, date.format(DateTimeFormatter.BASIC_ISO_DATE));
    }
}

