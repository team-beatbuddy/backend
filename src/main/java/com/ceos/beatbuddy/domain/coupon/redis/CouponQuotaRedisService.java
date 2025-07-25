package com.ceos.beatbuddy.domain.coupon.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponQuotaRedisService {
    private final RedisTemplate<String, String> redisTemplate;
    // TTL 최대 90일
    private static final long MAX_TTL_DAYS = 90L;

    /**
     * 쿠폰 쿼터를 Redis에 저장하고 TTL 설정
     */
    public void setQuota(Long couponId, List<Long> venueIds, int quota, LocalDate expireDate) {
        long ttlDays = Math.max(0, Math.min(ChronoUnit.DAYS.between(LocalDate.now(), expireDate), MAX_TTL_DAYS));
        if (ttlDays == 0) return; // 과거일 경우 Redis에 저장 안 함

        for (Long venueId : venueIds) {
            String key = CouponRedisKeyUtil.getQuotaKey(couponId, venueId, LocalDate.now());
            redisTemplate.opsForValue().set(key, String.valueOf(quota), ttlDays, TimeUnit.DAYS);
        }
    }

    public int getQuota(Long couponId, Long venueId, LocalDate date) {
        String key = CouponRedisKeyUtil.getQuotaKey(couponId, venueId, date);
        log.debug("Redis key for quota: {}", key);
        try {
            String value = redisTemplate.opsForValue().get(key);
            return value != null ? Integer.parseInt(value) : 0;
        } catch (Exception e) {
            return 0; // Redis 장애 시 기본값으로 처리
        }
    }

    /**
     * 유효기간 변경 시 TTL 재설정
     */
    public void updateQuotaTTL(Long couponId, List<Long> venueIds, LocalDate newExpireDate) {
        for (Long venueId : venueIds) {
            String key = CouponRedisKeyUtil.getQuotaKey(couponId, venueId, LocalDate.now());
            if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
                long ttlDays = Math.max(0, Math.min(ChronoUnit.DAYS.between(LocalDate.now(), newExpireDate), MAX_TTL_DAYS));
                if (ttlDays == 0) {
                    redisTemplate.delete(key); // 만료된 쿠폰은 삭제
                } else {
                    redisTemplate.expire(key, ttlDays, TimeUnit.DAYS);
                }
            }
        }
    }


    /**
     * quota가 0일 경우 key 삭제
     */
    public void deleteQuotaIfEmpty(Long venueId, Long couponId) {
        String key = CouponRedisKeyUtil.getQuotaKey(couponId, venueId, LocalDate.now());

        String value = redisTemplate.opsForValue().get(key);
        if ("0".equals(value)) {
            redisTemplate.delete(key);
        }
    }
}
