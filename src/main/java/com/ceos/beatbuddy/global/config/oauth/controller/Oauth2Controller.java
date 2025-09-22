package com.ceos.beatbuddy.global.config.oauth.controller;

import com.ceos.beatbuddy.domain.member.application.MemberService;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.code.SuccessCode;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import com.ceos.beatbuddy.global.config.oauth.application.Oauth2Service;
import com.ceos.beatbuddy.global.dto.ResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/oauth")
public class Oauth2Controller {

    private final Oauth2Service oauth2Service;
    private final MemberService memberService;

    // @GetMapping("/logout")
    // @Operation(summary = "로그아웃",
    //         description = "로그아웃을 진행합니다.\n"
    //                 + "로그아웃을 진행하면 세션이 종료되고 Refresh 토큰이 만료됩니다.\n"
    //                 + "로그아웃 후 다시 로그인을 진행해야 합니다")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "로그아웃에 성공했습니다. 본문은 로그아웃한 유저의 kakao id입니다."
    //                 , content = @Content(mediaType = "application/json",
    //                 schema = @Schema(implementation = String.class))
    //         ),
    //         @ApiResponse(responseCode = "400", description = "잘못된 토큰입니다"
    //                 + "\n에러 메시지가 출력됩니다.",
    //                 content = @Content(mediaType = "application/json",
    //                         schema = @Schema(implementation = String.class))),
    //         @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다",
    //                 content = @Content(mediaType = "application/json",
    //                         schema = @Schema(implementation = ResponseTemplate.class)))
    // })
    // public ResponseEntity<String> kakaoLogout(HttpSession session) {
    //     session.invalidate();
    //     Long memberId = SecurityUtils.getCurrentMemberId();
    //     String result = oauth2Service.logout(memberId);
    //     return ResponseEntity.ok(result);
    // }
    //
    // @PostMapping("/resign")
    // @Operation(summary = "회원탈퇴",
    //         description = "회원탈퇴를 진행합니다.\n"
    //                 + "회원탈퇴를 진행하면 유저의 대한 장르,지역,분위기들의 선호도와 하트비트,아카이브가 전부 삭제됩니다.\n"
    //                 + "탈퇴가 완료되면 로그아웃됩니다.")
    // @ApiResponses(value = {
    //         @ApiResponse(responseCode = "200", description = "회원탈퇴에 성공했습니다. 본문은 회원탈퇴한 유저의 kakao id입니다."
    //                 , content = @Content(mediaType = "application/json",
    //                 schema = @Schema(implementation = String.class))
    //         ),
    //         @ApiResponse(responseCode = "400", description = "잘못된 토큰입니다"
    //                 + "\n에러 메시지가 출력됩니다.",
    //                 content = @Content(mediaType = "application/json",
    //                         schema = @Schema(implementation = String.class))),
    //         @ApiResponse(responseCode = "404", description = "유저가 존재하지 않습니다",
    //                 content = @Content(mediaType = "application/json",
    //                         schema = @Schema(implementation = ResponseTemplate.class)))
    // })
    // public ResponseEntity<String> kakaoResign(HttpSession session) {
    //     session.invalidate();
    //     Long memberId = SecurityUtils.getCurrentMemberId();
    //     String result = oauth2Service.resign(memberId);
    //     return ResponseEntity.ok(result);
    // }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃을 처리합니다. Redis에서 refresh token을 삭제합니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "로그아웃 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<String>> logout() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        memberService.logout(memberId);
        return ResponseEntity
            .status(SuccessCode.SUCCESS_LOGOUT.getStatus().value())
            .body(new ResponseDTO<>(SuccessCode.SUCCESS_LOGOUT, "로그아웃 되었습니다."));
    }

    @DeleteMapping("/withdraw")
    @Operation(summary = "회원 탈퇴",
        description = "회원탈퇴를 진행합니다. 사용자의 모든 데이터(장르, 지역, 분위기 선호도, 하트비트, 아카이브)가 삭제됩니다.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "회원탈퇴 성공",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResponseDTO.class))),
        @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<ResponseDTO<String>> withdraw() {
        Long memberId = SecurityUtils.getCurrentMemberId();
        memberService.withdrawMember(memberId);
        return ResponseEntity
            .status(SuccessCode.SUCCESS_WITHDRAW.getStatus().value())
            .body(new ResponseDTO<>(SuccessCode.SUCCESS_WITHDRAW, "회원탈퇴가 완료되었습니다."));
    }



}