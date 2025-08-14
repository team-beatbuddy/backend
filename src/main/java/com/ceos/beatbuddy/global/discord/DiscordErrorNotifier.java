package com.ceos.beatbuddy.global.discord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class DiscordErrorNotifier {
    private final WebClient webClient;
    private final String webhook;

    // ✅ 명시적 생성자 + Qualifier로 defaultWebClient 주입
    public DiscordErrorNotifier(
            @Qualifier("defaultWebClient") WebClient webClient,
            @Value("${discord.webhook}") String webhook
    ) {
        this.webClient = webClient;
        this.webhook = webhook;
    }

    public Mono<Void> send(ErrorNotice n) {
        Map<String, Object> embed = Map.of(
                "title", String.format("[%s] %s %s (%d)", n.env(), n.method(), n.path(), n.status()),
                "color", 15158332, // 빨강
                "fields", new Object[]{
                        Map.of("name", "Member ID", "value", String.valueOf(n.memberId()), "inline", true),
                        Map.of("name", "Reason", "value", n.reason(), "inline", true),
                        Map.of("name", "Message", "value", n.message(), "inline", false),
                        Map.of("name", "Trace ID", "value", String.valueOf(n.traceId()), "inline", false),
                        Map.of("name", "At", "value", n.occurredAt(), "inline", false)
                }
        );

        Map<String, Object> payload = Map.of(
                "embeds", new Object[]{embed}
        );

        return webClient.post()
                .uri(webhook)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class);
    }
}