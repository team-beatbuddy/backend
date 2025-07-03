package com.ceos.beatbuddy.global.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.util.Base64;

@Configuration
public class ElasticsearchConfig {

    @Value("${spring.data.elasticsearch.uris}")
    private String elasticsearchUri;

    @Value("${spring.data.elasticsearch.api-key}")
    private String apiKey;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        URI uri = URI.create(elasticsearchUri);

        RestClientBuilder builder = RestClient.builder(
                        new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()))
                .setDefaultHeaders(new org.apache.http.Header[]{
                        new org.apache.http.message.BasicHeader("Authorization", "ApiKey " + apiKey)
                });

        RestClient restClient = builder.build();

        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper());

        return new ElasticsearchClient(transport);
    }
}