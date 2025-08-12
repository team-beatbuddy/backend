package com.ceos.beatbuddy.global.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.Charset;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
public class DanalConfig {

    private static final Charset EUC_KR = Charset.forName("EUC-KR");
    public static Map<String, String> parseResponse(String result) {
        // Danal 응답은 "KEY=VALUE" 형태로 되어있음
        String[] pairs = result.split("&");
        Map<String, String> resultMap = new java.util.HashMap<>();
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            if (keyValue.length == 2) {
                resultMap.put(keyValue[0], keyValue[1]);
            } else {
                resultMap.put(keyValue[0], ""); // 값이 없는 경우 빈 문자열로 처리
            }
        }
        return resultMap;
    }

    @Bean
    public RestTemplate danalRestTemplate(RestTemplateBuilder builder) {
        // 메시지 컨버터: Form + String 모두 EUC-KR로
        FormHttpMessageConverter form = new FormHttpMessageConverter();
        form.setCharset(EUC_KR);

        StringHttpMessageConverter stringConv = new StringHttpMessageConverter(EUC_KR);

        List converters = new ArrayList<>();
        // 순서 중요: form 우선
        converters.add(form);
        converters.add(stringConv);

        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .additionalMessageConverters(converters)
                .build();
    }
}