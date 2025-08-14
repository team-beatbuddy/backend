package com.ceos.beatbuddy.domain.venue.kakaoMap;

import com.ceos.beatbuddy.global.CustomException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

@Service
public class KakaoLocalClient {

    private final KakaoConfig kakaoConfig;
    private final WebClient webClient;

    public KakaoLocalClient(
            KakaoConfig kakaoConfig,
            @Qualifier("kakaoWebClient") WebClient webClient // ← 명시!
    ) {
        this.kakaoConfig = kakaoConfig;
        this.webClient = webClient;
    }

    public Mono<CoordinateResponse> getCoordinateFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return Mono.error(new CustomException("주소가 비어있습니다."));
        }
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
                    try {
                        double x = Double.parseDouble(doc.getX());
                        double y = Double.parseDouble(doc.getY());
                        return Mono.just(new CoordinateResponse(x, y));
                    } catch (NumberFormatException e) {
                        return Mono.error(new CustomException("좌표 형식이 올바르지 않습니다: " + e.getMessage()));
                    }
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
