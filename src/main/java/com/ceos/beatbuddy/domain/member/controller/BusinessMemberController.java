package com.ceos.beatbuddy.domain.member.controller;

import com.ceos.beatbuddy.domain.member.application.BusinessMemberService;
import com.ceos.beatbuddy.domain.member.dto.*;
import com.ceos.beatbuddy.global.ResponseTemplate;
import com.ceos.beatbuddy.global.config.jwt.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
@Tag(name = "Business Controller", description = "비즈니스 사용자 컨트롤러\n")
//        + "현재는 회원가입 관련 로직만 작성되어 있습니다\n"
//        + "추후 사용자 상세 정보, 아카이브를 조회하는 기능이 추가될 수 있습니다")
public class BusinessMemberController {
    private final BusinessMemberService businessMemberService;

    @PostMapping("/verify")
    @Operation(summary = "비즈니스 사용자 본인 확인", description = "번호, 주민번호 7자리, 실명으로 본인 인증 요청을 진행하는 과정입니다. \n 현재는 테스트 과정이므로 인증번호 없이 바로 통과됩니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = ""
                    , content = @Content(mediaType = "application/json"
                    , schema = @Schema(implementation = MemberResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<BusinessMemberResponseDTO> verifyBusinessMember(@Valid @RequestBody BusinessMemberDTO dto) {
        Long memberId = SecurityUtils.getCurrentMemberId();
        BusinessMemberResponseDTO result = businessMemberService.businessMemberSignup(memberId, dto);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-code")
    @Operation(summary = "비즈니스 사용자 전화번호 확인, 인증번호 요청", description = "테스트 과정입니다. 인증번호를 응답합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "인증번호 반환",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = VerificationCodeResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "요청한 유저가 존재하지 않습니다",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ResponseTemplate.class)))
    })
    public ResponseEntity<VerificationCodeResponseDTO> verifyPhoneNumber(@Valid @RequestBody VerifyPhoneNumberDTO dto) {
        //Long memberId = SecurityUtils.getCurrentMemberId();
        System.out.println("현재 여기");
        VerificationCodeResponseDTO result = businessMemberService.sendVerificationCode(dto.getPhoneNumber());

        return ResponseEntity.ok(result);
    }
}
