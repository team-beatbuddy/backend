package com.ceos.beatbuddy.global.config.jwt.redis;

import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
    RefreshToken findByUserId(Long userId);
    List<RefreshToken> findAllByUserId(Long userId);
    void deleteAllByUserId(Long userId);
}
