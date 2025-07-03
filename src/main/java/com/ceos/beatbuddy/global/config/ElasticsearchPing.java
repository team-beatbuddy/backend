package com.ceos.beatbuddy.global.config;


import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

@Component
@Slf4j
public class ElasticsearchPing {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.api-key}")
    private String apiKey;

    @PostConstruct
    public void ping() {
        try {
            URI uri = URI.create(elasticsearchUri);
            String host = uri.getHost();
            int port = uri.getPort();
            String scheme = uri.getScheme();

            RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                    .setDefaultHeaders(new org.apache.http.Header[]{
                            new BasicHeader("Authorization", "ApiKey " + apiKey)
                    });

            try (RestClient client = builder.build()) {
                Response response = client.performRequest(new Request("GET", "/"));
                log.info("✅ Elasticsearch 연결 성공: " + response.getStatusLine());
            }
        } catch (IOException e) {
            log.error("❌ Elasticsearch 연결 실패: " + e.getMessage());
            // 필요에 따라 애플리케이션 시작 실패 여부 결정
        }
    }
        URI uri = URI.create(elasticsearchUri);
        String host = uri.getHost();
        int port = uri.getPort();
        String scheme = uri.getScheme();

        RestClientBuilder builder = RestClient.builder(new HttpHost(host, port, scheme))
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                });

        try (RestClient client = builder.build()) {
            Response response = client.performRequest(new Request("GET", "/"));
            log.info("✅ Elasticsearch 연결 성공: " + response.getStatusLine());
        }
    }
}