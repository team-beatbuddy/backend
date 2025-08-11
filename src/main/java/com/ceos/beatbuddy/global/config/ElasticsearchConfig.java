package com.ceos.beatbuddy.global.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.api-key}")
    private String apiKey;

    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        URI uri = URI.create(elasticsearchUri);
        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Authorization", "ApiKey " + apiKey)
                });
        return builder.build();
    }

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        RestClient client = restClient(); // 여기 직접 호출 (생성자 주입 X)

        ObjectMapper objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS); // ISO 8601 포맷으로 출력

        JacksonJsonpMapper jsonpMapper = new JacksonJsonpMapper(objectMapper);

        ElasticsearchTransport transport = new RestClientTransport(client, jsonpMapper);
        return new ElasticsearchClient(transport);
    }
}