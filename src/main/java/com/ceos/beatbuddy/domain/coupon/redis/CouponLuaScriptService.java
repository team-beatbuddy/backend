package com.ceos.beatbuddy.domain.coupon.redis;

import com.ceos.beatbuddy.domain.coupon.domain.Coupon;
import com.ceos.beatbuddy.domain.coupon.exception.CouponErrorCode;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.code.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;

@RequiredArgsConstructor
@Service
public class CouponLuaScriptService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String LUA_SCRIPT = """
        local stock = tonumber(redis.call('GET', KEYS[1]))
        local decrement = tonumber(ARGV[1])

        if stock == nil then
          return -1
        end

        if stock < decrement then
          return 0
        end

        redis.call('DECRBY', KEYS[1], decrement)
        return 1
        """;

    public void decreaseQuotaOrThrow(String redisKey) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), "1");
        int code = result != null ? result.intValue() : -2;

        switch (code) {
            case -1 -> throw new CustomException(CouponErrorCode.COUPON_QUOTA_NOT_INITIALIZED);
            case 0  -> throw new CustomException(CouponErrorCode.COUPON_QUOTA_SOLD_OUT);
            case 1  -> {} // 정상 통과
            default -> throw new CustomException(CouponErrorCode.COUPON_SERVER_ERROR);
        }
    }

    public enum LuaResult {
        NOT_INITIALIZED,
        SOLD_OUT,
        SUCCESS,
        UNKNOWN
    }
}