package com.ceos.beatbuddy.domain.coupon.redis;

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

    public LuaResult decreaseQuota(String redisKey) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, Collections.singletonList(redisKey), "1");

        return switch (result != null ? result.intValue() : -2) {
            case -1 -> LuaResult.NOT_INITIALIZED;
            case 0 -> LuaResult.SOLD_OUT;
            case 1 -> LuaResult.SUCCESS;
            default -> LuaResult.UNKNOWN;
        };
    }

    public enum LuaResult {
        NOT_INITIALIZED,
        SOLD_OUT,
        SUCCESS,
        UNKNOWN
    }
}