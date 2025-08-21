package com.ceos.beatbuddy.global.config;

import com.ceos.beatbuddy.global.config.jwt.JwtFilter;
import com.ceos.beatbuddy.global.config.jwt.TokenProvider;
import com.ceos.beatbuddy.global.config.oauth.OAuth2SuccessHandler;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
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
@Slf4j
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
                                "http://localhost:3000/**","/admin/**", "/fcm-test.html", "/fcm-simple-test.html", "/firebase-messaging-sw.js", "/firebase-cloud-messaging-push-scope").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/venues/**").permitAll()  // 조회 전용 공개
                        .requestMatchers("/api/v1/venues/coordinates/**").authenticated()  // 좌표 관리 API는 인증 필요
                        .requestMatchers("/api/v1/venues/**").authenticated()             // 나머지 베뉴 관리는 인증 필요
                        .anyRequest().authenticated())
                // 인증 실패 시 401 에러 반환
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"UNAUTHORIZED\",\"message\":\"인증이 필요합니다.\"}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"error\":\"FORBIDDEN\",\"message\":\"접근 권한이 없습니다.\"}");
                        }))
                //oauth2
                .oauth2Login(oauth2 -> oauth2
                        .clientRegistrationRepository(clientRegistrationRepository)
                        .userInfoEndpoint(endpoint -> endpoint
                                .userService(oAuth2UserService)
                                .oidcUserService(customOidcUserService())
                        )
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler((request, response, exception) -> {
                            if (exception instanceof OAuth2AuthenticationException ex) {
                                OAuth2Error error = ex.getError();
                                log.warn("OAuth2 login failure code={}, desc={}", error.getErrorCode(), error.getDescription());
                            }
                            response.sendRedirect("/login?error");
                        })
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

            OidcUserInfo userInfo = new OidcUserInfo(claims);

            return new DefaultOidcUser(
                    oidcUser.getAuthorities(),
                    oidcUser.getIdToken(),
                    userInfo,
                    "sub"
            );
        };
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> {
            web.ignoring().requestMatchers("/error","/login", "/join", 
                "/fcm-test.html", "/firebase-messaging-sw.js", 
                "/css/**", "/js/**", "/images/**", "/static/**");
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
