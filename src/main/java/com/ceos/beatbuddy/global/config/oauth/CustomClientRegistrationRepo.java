package com.ceos.beatbuddy.global.config.oauth;

import com.ceos.beatbuddy.global.config.AppleClientSecretUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;

@Configuration
@Slf4j
public class CustomClientRegistrationRepo {

    @Value("${kakao.client-id}")
    private String kakaoClientId;
    @Value("${kakao.client-secret}")
    private String kakaoClientSecret;
    @Value("${google.client-id}")
    private String googleClientId;
    @Value("${google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.apple.client-id}")
    private String appleClientId;
    @Value("${apple.team-id}")
    private String appleTeamId;
    @Value("${apple.private-key}")
    private String applePrivate;
    @Value("${apple.key-id}")
    private String appleKeyId;


    public ClientRegistration kakaoClientRegistration() {
        return ClientRegistration.withRegistrationId("kakao")
                .clientId(kakaoClientId)
                .clientSecret(kakaoClientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.beatbuddy.world/login/oauth2/code/kakao")
                .authorizationUri("https://kauth.kakao.com/oauth/authorize")
                .tokenUri("https://kauth.kakao.com/oauth/token")
                .userInfoUri("https://kapi.kakao.com/v2/user/me")
                .userNameAttributeName("kakao_account").build();
    }

    public ClientRegistration googleClientRegistration() {
        return ClientRegistration.withRegistrationId("google")
                .clientId(googleClientId)
                .clientSecret(googleClientSecret)
                .clientAuthenticationMethod(org.springframework.security.oauth2.core.ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.beatbuddy.world/login/oauth2/code/google")
                .scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo")
                .userNameAttributeName("sub")
                .clientName("Google")
                .build();
    }

    public ClientRegistration appleClientRegistration() {
        String clientSecret = AppleClientSecretUtil.generateClientSecretFromString(
                appleTeamId,
                appleClientId,
                appleKeyId,
                applePrivate
        );

        return ClientRegistration.withRegistrationId("apple")
                .clientId(appleClientId)
                .clientSecret(clientSecret) // ✅ JWT 기반 secret
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.beatbuddy.world/login/oauth2/code/apple")
                .authorizationUri("https://appleid.apple.com/auth/authorize?response_mode=query")
                .tokenUri("https://appleid.apple.com/auth/token")
                .jwkSetUri("https://appleid.apple.com/auth/keys")
                .scope("openid")
                .userNameAttributeName("sub")
                .clientName("Apple")
                .build();
    }

    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        log.info("Creating custom client registration repository...");
        return new InMemoryClientRegistrationRepository(
                googleClientRegistration(),
                kakaoClientRegistration(),
                appleClientRegistration()
        );
    }
}
