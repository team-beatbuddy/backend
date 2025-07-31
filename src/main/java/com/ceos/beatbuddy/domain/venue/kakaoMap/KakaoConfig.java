package com.ceos.beatbuddy.domain.venue.kakaoMap;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kakao")
@Getter
public class KakaoConfig {
    @Value("${kakao.client-id}")
    private String restApiKey;
}