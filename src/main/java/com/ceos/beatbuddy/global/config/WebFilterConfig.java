package com.ceos.beatbuddy.global.config;

import com.ceos.beatbuddy.global.discord.CachingRequestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class WebFilterConfig {

    @Bean
    public FilterRegistrationBean<CachingRequestFilter> cachingRequestFilter() {
        var reg = new FilterRegistrationBean<>(new CachingRequestFilter());
        reg.addUrlPatterns("/*");
        // 보안 필터(Spring Security) 뒤에서 실행되도록 낮은 우선순위 권장
        reg.setOrder(Ordered.LOWEST_PRECEDENCE - 10);
        return reg;
    }
}