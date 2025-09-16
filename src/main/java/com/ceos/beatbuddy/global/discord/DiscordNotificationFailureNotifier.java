package com.ceos.beatbuddy.global.discord;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class DiscordNotificationFailureNotifier {
    private final WebClient webClient;
    private final String webhook;

    public DiscordNotificationFailureNotifier(
            @Qualifier("defaultWebClient") WebClient webClient,
            @Value("${discord.webhook}") String webhook
    ) {
        this.webClient = webClient;
        this.webhook = webhook;
    }

    @Async
    public void sendNotificationFailure(String token, String title, String body, String reason) {
        String maskedToken = maskToken(token);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        Map<String, Object> embed = Map.of(
                "title", "🔥 FCM 알림 전송 실패",
                "color", 16711680, // 빨강
                "fields", new Object[]{
                        Map.of("name", "Token", "value", "```" + maskedToken + "```", "inline", false),
                        Map.of("name", "Title", "value", emptyToDash(title), "inline", true),
                        Map.of("name", "Body", "value", trimForDiscord(body), "inline", true),
                        Map.of("name", "Reason", "value", "```" + emptyToDash(reason) + "```", "inline", false),
                        Map.of("name", "Time", "value", timestamp, "inline", true)
                }
        );

        Map<String, Object> payload = Map.of("embeds", new Object[]{embed});

        webClient.post()
                .uri(webhook)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe(
                        success -> {},
                        error -> System.err.println("Discord 알림 실패: " + error.getMessage())
                );
    }

    private String maskToken(String token) {
        if (token == null || token.length() < 10) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }

    private static String emptyToDash(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private static String trimForDiscord(String s) {
        if (s == null) return "-";
        int max = 200; // 알림 내용은 짧게
        return (s.length() > max) ? s.substring(0, max) + "..." : s;
    }
}