package com.ceos.beatbuddy.domain.venue.kakaoMap;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
public class KakaoApiResponse {
    private List<Document> documents;

    @Getter
    @Setter
    public static class Document {
        private String x; // longitude
        private String y; // latitude
        private String address_name;
    }
}
