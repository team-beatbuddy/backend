package com.ceos.beatbuddy.domain.coupon.redis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CouponRedisKeyUtil {

    public static String getQuotaKey(Long couponId, LocalDate date) {
        return String.format("venue_coupon_quota:%d:%s",
                couponId, date.format(DateTimeFormatter.BASIC_ISO_DATE));
    }
}

