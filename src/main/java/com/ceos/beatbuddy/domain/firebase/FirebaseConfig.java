package com.ceos.beatbuddy.domain.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.private_key}")
    private String privateKey;

    @Value("${firebase.client_email}")
    private String clientEmail;

    @Value("${firebase.project_id}")
    private String projectId;

    @Value("${firebase.private_key_id}")
    private String privateKeyId;

    @Value("${firebase.client_id}")
    private String clientId;

    @PostConstruct
    public void initializeFirebase() throws IOException {
        GoogleCredentials credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(buildFirebaseConfigJson().getBytes(StandardCharsets.UTF_8))
        );

        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(credentials)
                .setProjectId(projectId)
                .build();

        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }

    private String buildFirebaseConfigJson() {
        return """
        {
          "type": "service_account",
          "project_id": "%s",
          "private_key_id": "%s",
          "private_key": "%s",
          "client_email": "%s",
          "client_id": "%s"
        }
        """.formatted(projectId, privateKeyId, privateKey.replace("\\n", "\n"), clientEmail, clientId);
    }
}