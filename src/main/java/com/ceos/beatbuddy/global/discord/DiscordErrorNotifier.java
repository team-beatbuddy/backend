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

    public DiscordErrorNotifier(
            @Qualifier("defaultWebClient") WebClient webClient,
            @Value("${discord.webhook}") String webhook
    ) {
        this.webClient = webClient;
        this.webhook = webhook;
    }

    public Mono<Void> send(ErrorNotice n) {
        String query   = emptyToDash(n.queryString());
        String body    = trimForDiscord(n.bodyPreview());        // 길이 제한
        String client  = (emptyToDash(n.clientIp()) + "\n" + emptyToDash(n.userAgent())).trim();


        Map<String, Object> embed = Map.of(
                "title", String.format("[%s] %s %s (%d)", n.env(), n.method(), n.path(), n.status()),
                "color", 15158332, // 빨강
                "fields", new Object[]{
                        Map.of("name", "Member ID", "value", String.valueOf(n.memberId()), "inline", true),
                        Map.of("name", "Reason", "value", emptyToDash(n.reason()), "inline", true),
                        Map.of("name", "Message", "value", emptyToDash(n.message()), "inline", false),
                        Map.of("name", "Client", "value", "```" + client + "```", "inline", false),
                        Map.of("name", "Query",  "value", "```" + query + "```", "inline", false),
                        Map.of("name", "Body",   "value", "```json\n" + body + "\n```", "inline", false),
                        Map.of("name", "Trace ID", "value", String.valueOf(n.traceId()), "inline", false),
                        Map.of("name", "At",       "value", n.occurredAt(), "inline", false)
                }
        );

        Map<String, Object> payload = Map.of("embeds", new Object[]{embed});

        return webClient.post()
                .uri(webhook)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class);
    }

    private static String emptyToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String trimForDiscord(String s) {
        if (s == null) return "-";
        // 임베드 필드 value는 1024자 제한 → 여유를 두고 자르기
        int max = 950;
        return (s.length() > max) ? s.substring(0, max) + "…(+truncated)" : s;
    }
}
