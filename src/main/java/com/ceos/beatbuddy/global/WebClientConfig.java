package com.ceos.beatbuddy.global;

import com.ceos.beatbuddy.domain.venue.kakaoMap.KakaoConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("defaultWebClient")
    public WebClient defaultWebClient(WebClient.Builder builder) {
        return builder.build();
    }

    // 공통 HttpClient (커넥션/응답 타임아웃 등)
    @Bean
    public ReactorClientHttpConnector reactorClientHttpConnector() {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(5)); // 응답 타임아웃
        return new ReactorClientHttpConnector(httpClient);
    }


    // 카카오 전용 WebClient
    @Bean
    @Qualifier("kakaoWebClient")
    public WebClient kakaoWebClient(WebClient.Builder builder, KakaoConfig kakaoConfig) {
        return builder
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoConfig.getClientId())
                .build();
    }
}