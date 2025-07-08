package com.ceos.beatbuddy.domain.coupon.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class CouponQuotaRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    // TTL 최대 90일
    private static final long MAX_TTL_DAYS = 90L;

    /**
     * 쿠폰 쿼터를 Redis에 저장하고 TTL 설정
     */
    public void setQuota(Long couponId, int quota, LocalDate expireDate) {
        String key = CouponRedisKeyUtil.getQuotaKey(couponId, LocalDate.now());

        long ttlDays = Math.min(ChronoUnit.DAYS.between(LocalDate.now(), expireDate), MAX_TTL_DAYS);

        redisTemplate.opsForValue().set(key, String.valueOf(quota), ttlDays, TimeUnit.DAYS);
    }

    /**
     * 유효기간 변경 시 TTL 재설정
     */
    public void updateQuotaTTL(Long couponId, LocalDate newExpireDate) {
        String key = CouponRedisKeyUtil.getQuotaKey(couponId, LocalDate.now());

        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            long ttlDays = Math.min(ChronoUnit.DAYS.between(LocalDate.now(), newExpireDate), MAX_TTL_DAYS);
            redisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
        }
    }


    /**
     * quota가 0일 경우 key 삭제
     */
    public void deleteQuotaIfEmpty(Long couponId) {
        String key = CouponRedisKeyUtil.getQuotaKey(couponId, LocalDate.now());

        String value = redisTemplate.opsForValue().get(key);
        if ("0".equals(value)) {
            redisTemplate.delete(key);
        }
    }
}
