package com.ceos.beatbuddy.global.config.oauth;

import com.ceos.beatbuddy.domain.member.constant.Role;
import com.ceos.beatbuddy.domain.member.entity.Member;
import com.ceos.beatbuddy.domain.member.repository.MemberRepository;
import com.ceos.beatbuddy.global.config.jwt.TokenProvider;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshToken;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshTokenRepository;
import com.ceos.beatbuddy.global.config.oauth.dto.LoginResponseDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {

        String username;
        Long memberId;
        String name;
        String role;

        // ✅ principal 타입별 분기 처리
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User oAuth2User) {
            username = oAuth2User.getUsername();
            memberId = oAuth2User.getMemberId();
            name = oAuth2User.getName();

        } else if (principal instanceof org.springframework.security.oauth2.core.oidc.user.OidcUser oidcUser) {
            // Apple 로그인 처리
            username = oidcUser.getAttribute("email"); // or "sub"
            memberId = 0L; // 필요시 DB에서 연동
            name = oidcUser.getAttribute("name");
            if (name == null) name = "AppleUser";

            // ✅ 여기에 Apple 사용자 저장/조회 추가
            String finalName = name;
            String appleLoginId = "apple_" + username;
            Member member = memberRepository.findByLoginId(appleLoginId)
                    .orElseGet(() -> memberRepository.save(
                            Member.builder()
                                    .loginId(appleLoginId)
                                    .nickname(finalName)
                                    .role(Role.USER)
                                    .build()
                    ));

            memberId = member.getId(); // 🔥 실제 DB에서 가져온 ID로 설정


        } else {
            log.error("Unsupported principal type: {}", principal.getClass());
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported principal");
            return;
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority auth = iterator.hasNext() ? iterator.next() : () -> "ROLE_USER";
        role = auth.getAuthority();

        String access = tokenProvider.createToken("access", memberId, username, role, 1000 * 60 * 60 * 2L);
        String refresh = tokenProvider.createToken("refresh", memberId, username, role, 1000 * 3600 * 24 * 14L);

        saveRefreshToken(memberId, refresh);

        String uri = request.getRequestURI(); // ex: /login/oauth2/code/apple
        String[] segments = uri.split("/");
        String provider = segments.length > 0 ? segments[segments.length - 1] : "unknown";

        if (!provider.equals("google") && !provider.equals("kakao") && !provider.equals("apple")) {
            log.warn("Unsupported OAuth2 provider: {}", provider);
            provider = "kakao";
        }

        String redirectUrl = "https://beatbuddy.world/login/oauth2/callback/" + provider + "?access=" + access;

        LoginResponseDto loginResponseDto = LoginResponseDto.builder()
                .memberId(memberId)
                .loginId(username)
                .username(name)
                .accessToken(access)
                .refreshToken(refresh)
                .build();

        log.info("saved: " + access);

        ResponseCookie cookie = ResponseCookie.from("refresh", refresh)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .maxAge(60 * 60 * 24 * 14)
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = objectMapper.writeValueAsString(loginResponseDto);
        response.getWriter().write(jsonResponse);

        HttpSession session = request.getSession();
        session.setMaxInactiveInterval(600);

        if (!response.isCommitted()) {
            response.sendRedirect(redirectUrl);
        }
    }

    private void saveRefreshToken(Long userId, String refresh) {

        RefreshToken refreshToken = new RefreshToken(refresh, userId);

        RefreshToken saved = refreshTokenRepository.save(refreshToken);

        log.info("saved: " + saved.getRefreshToken());
    }
}
