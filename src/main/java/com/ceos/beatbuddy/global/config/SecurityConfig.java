package com.ceos.beatbuddy.global.config;

import com.ceos.beatbuddy.global.config.jwt.JwtFilter;
import com.ceos.beatbuddy.global.config.jwt.TokenProvider;
import com.ceos.beatbuddy.global.config.oauth.CustomClientRegistrationRepo;
import com.ceos.beatbuddy.global.config.oauth.OAuth2SuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2LoginAuthenticationFilter;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final ClientRegistrationRepository clientRegistrationRepository;
    private final DefaultOAuth2UserService oAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final TokenProvider tokenProvider;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors
                        .configurationSource(corsConfigurationSource()))
                .csrf(CsrfConfigurer::disable)
                .formLogin((auth) -> auth.disable())
                .httpBasic(HttpBasicConfigurer::disable)
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                //Custom filter 추가
                .addFilterAfter(new JwtFilter(tokenProvider), OAuth2LoginAuthenticationFilter.class)
                // 경로에 대한 권한 부여
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/reissue", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html",
                                "http://localhost:3000/**","/admin/**").permitAll()
                        .anyRequest().authenticated())
                //oauth2
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(oAuth2UserService)
                                .oidcUserService(customOidcUserService())
                        )
                        .successHandler(oAuth2SuccessHandler)
                );

        return http.build();
    }
    @Bean
    public OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService() {
        return userRequest -> {
            OidcUserService delegate = new OidcUserService();
            OidcUser oidcUser = delegate.loadUser(userRequest);

            Map<String, Object> claims = new HashMap<>(oidcUser.getClaims());
            claims.putIfAbsent("name", "AppleUser");
            claims.putIfAbsent("email", "unknown@apple.com");

            return new DefaultOidcUser(oidcUser.getAuthorities(), userRequest.getIdToken(), "sub");
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().requestMatchers("/error","/login", "/join");
        };
    }

    protected CorsConfigurationSource corsConfigurationSource() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**",
                getDefaultCorsConfiguration());

        return source;
    }

    private CorsConfiguration getDefaultCorsConfiguration() {
        CorsConfiguration config = new CorsConfiguration();
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        return config;
    }

}
