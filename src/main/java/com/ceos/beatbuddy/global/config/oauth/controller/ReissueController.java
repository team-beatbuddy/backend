package com.ceos.beatbuddy.global.config.oauth.controller;

import com.ceos.beatbuddy.domain.member.application.ReissueService;
import com.ceos.beatbuddy.global.CustomException;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.config.jwt.TokenProvider;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshToken;
import com.ceos.beatbuddy.global.config.jwt.redis.RefreshTokenRepository;
import com.ceos.beatbuddy.global.config.oauth.dto.TokenResponseDto;
import com.ceos.beatbuddy.global.config.oauth.exception.OauthErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reissue Controller", description = "Token 만료 시 새로운 Access, Refresh 토큰을 재발급하는 컨트롤러")
public class ReissueController {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ReissueService reissueService;

    @PostMapping("/reissue")
    @Operation(summary = "토큰 재발급",
            description = "Access 토큰이 만료된 경우, Refresh 토큰으로 재발급합니다.\n"
                    + "Access 토큰과 Refresh 토큰을 모두 응답 본문에 담아 반환합니다.\n"
                    + "Refresh 토큰이 만료됐거나 유효하지 않은 토큰일 경우 에러를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "토큰을 재발급하는데 성공했습니다."
                    , content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TokenResponseDto.class))
            ),
            @ApiResponse(responseCode = "400", description = "잘못된 토큰입니다"
                    + "에러 메시지가 출력됩니다.",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "404", description = "토큰을 찾을 수 없습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<TokenResponseDto> reissue(HttpServletRequest request) {
        // Authorization 헤더에서 refresh token 추출 (Bearer 토큰 방식)
        String authHeader = request.getHeader("Authorization");
        String refresh = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            refresh = authHeader.substring(7);
        }

        if (refresh == null) {
            throw new CustomException(OauthErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        try {
            tokenProvider.isExpired(refresh);
        } catch (Exception e) {
            throw new CustomException(OauthErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        String category = tokenProvider.getCategory(refresh);

        if (!category.equals("refresh")) {
            throw new CustomException(OauthErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedRefresh = refreshTokenRepository.findById(refresh)
                .orElseThrow(() -> new CustomException(OauthErrorCode.REFRESH_TOKEN_NOT_FOUND));

        Long userId = savedRefresh.getUserId();

        /**
         * QA기간 동안만 주석처리 예정
         */
//        if(!savedRefresh.getUserId().equals(userId)){
//            return new ResponseEntity<>("Not Token Owner", HttpStatus.BAD_REQUEST);
//        }

        Long memberIdFromToken = tokenProvider.getMemberId(refresh);
        if (!userId.equals(memberIdFromToken)) {
            throw new CustomException(OauthErrorCode.INVALID_REFRESH_TOKEN);
        }

        String username = tokenProvider.getUsername(refresh);
        String role = tokenProvider.getRole(refresh);

        String newAccess = tokenProvider.createToken("access", userId, username, role, 1000 * 60 * 60 * 2L);
        String newRefresh = tokenProvider.createToken("refresh", userId, username, role, 1000 * 3600 * 24 * 14L);

        reissueService.deleteRefreshToken(refresh);
        reissueService.saveRefreshToken(userId, newRefresh);

        TokenResponseDto tokenResponseDto = new TokenResponseDto(newAccess, newRefresh);

        return ResponseEntity.ok(tokenResponseDto);
    }

}
