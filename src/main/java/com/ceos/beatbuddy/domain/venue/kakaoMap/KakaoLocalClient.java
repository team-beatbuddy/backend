package com.ceos.beatbuddy.domain.venue.kakaoMap;

import com.ceos.beatbuddy.global.CustomException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class KakaoLocalClient {

    private final KakaoConfig kakaoConfig;
    private WebClient webClient;
    @PostConstruct
    public void init() {
        this.webClient = WebClient.builder()
                .baseUrl("https://dapi.kakao.com")
                .defaultHeader(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoConfig.getRestApiKey())
                .build();
    }
    public Mono<CoordinateResponse> getCoordinateFromAddress(String address) {
        String realAddress = cleanAddress(address);
        String uri = UriComponentsBuilder
                .fromPath("/v2/local/search/address.json")
                .queryParam("query", realAddress)
                .build()
                .toString();

        return webClient.get()
                .uri(uri)
                .retrieve()
                .bodyToMono(KakaoApiResponse.class)
                .flatMap(response -> {
                    if (response.getDocuments().isEmpty()) {
                        return Mono.error(new CustomException("좌표를 찾을 수 없습니다."));
                    }
                    KakaoApiResponse.Document doc = response.getDocuments().get(0);
                    double x = Double.parseDouble(doc.getX());
                    double y = Double.parseDouble(doc.getY());
                    return Mono.just(new CoordinateResponse(x, y));
                });
    }

    private String cleanAddress(String address) {
        // 층, 호, F, B1, 301호 등 제거
        return address.replaceAll("\\s+\\d+(F|층|호)", "") // '3F', '301호' 등 제거
                .replaceAll("\\s+B\\d+F", "")       // 'B1F' 같은 지하 제거
                .replaceAll("\\s+[1-9]층", "")
                .replaceAll("\\s+\\d+호", "")
                .trim();
    }

}
